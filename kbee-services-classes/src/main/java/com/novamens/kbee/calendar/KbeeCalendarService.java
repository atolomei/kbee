package com.novamens.kbee.calendar;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.novamens.beans.BeansService;
import com.novamens.calendar.CalendarService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 * Non Workable days are per {@link Domain}
 */
public class KbeeCalendarService implements CalendarService, EventListener {

    static private final long CACHE_DURATION = 1000 * 60 * 60 * 6; // 6 horas

    // Dates must have the format: month/day or month/day/year
    static private String default_non_workable = "12/24; 12/25; 12/31; 1/1";

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeCalendarService.class.getName());

    static private Map<String, List<LocalDate>> dm_nw = new ConcurrentHashMap<String, List<LocalDate>>();;
    static private Map<String, Map<String, LocalDate>> dm_nw_map = new ConcurrentHashMap<String, Map<String, LocalDate>>();;
    static private Map<String, Instant> last_generated = new ConcurrentHashMap<String, Instant>();

    private Domain domain = null;

    private Integer CUTOFF_TIME;

    private Double START_HOUR_WORKABLE_DAY;
    private Double END_HOUR_WORKABLE_DAY;

    // private Double START_HOUR_WORKABLE_DAY;
    // private Double END_HOUR_WORKABLE_DAY;

    static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit) {
        return unit.between(d1, d2);
    }

    public KbeeCalendarService() {
    }

    public KbeeCalendarService(Domain domain) {
        this.domain = domain;
    }

    /**
     * Recorro las horas laborables desde start hasta end sumando uno
     */
    @Override
    public double getBusinessHoursDuration(OffsetDateTime started, OffsetDateTime ended) {

        if (getDomain() == null)
            throw new IllegalArgumentException("Domain is null");

        if (started == null || ended == null)
            return 0.0;

        logger.debug("--------------------------");

        String tz = getDomain().getTimeZone();
        Locale lo = getDomain().getLocale();

        logger.debug("Start    -> " + ServiceLocator.getService(DateTimeService.class).format(started, tz, lo,
                DateTimeService.Dow_Month_Day_Year_hh_mm));
        logger.debug("End      -> "
                + ServiceLocator.getService(DateTimeService.class).format(ended, tz, lo, DateTimeService.Dow_Month_Day_Year_hh_mm));

        double hours = 0.0;

        try {

            DateTimeService service = ServiceLocator.getService(DateTimeService.class);
            String zid = null;

            if (getDomain().getTimeZone() != null)
                zid = service.getMapZoneIds().get(getDomain().getTimeZone());

            if (zid == null)
                zid = ZoneId.systemDefault().getId();

            ZonedDateTime zoned_started = ZonedDateTime.ofInstant(started.toInstant(), ZoneId.of(zid));
            ZonedDateTime zoned_ended = ZonedDateTime.ofInstant(ended.toInstant(), ZoneId.of(zid));

            if (!isWorkableZonedDateTime(zoned_started))
                zoned_started = getNextWorkableZonedDateTime(zoned_started);

            if (!isWorkableZonedDateTime(zoned_ended))
                zoned_ended = getNextWorkableZonedDateTime(zoned_ended);

            logger.debug("Normalized Start -> " + ServiceLocator.getService(DateTimeService.class).format(
                    zoned_started.withZoneSameInstant(ZoneId.of(zid)).toOffsetDateTime(), tz, lo,
                    DateTimeService.Dow_Month_Day_Year_hh_mm));
            logger.debug("Normalized End   -> " + ServiceLocator.getService(DateTimeService.class).format(
                    zoned_ended.withZoneSameInstant(ZoneId.of(zid)).toOffsetDateTime(), tz, lo,
                    DateTimeService.Dow_Month_Day_Year_hh_mm));
            logger.debug("Duration (hrs)   -> "
                    + Double.valueOf((zonedDateTimeDifference(zoned_started, zoned_ended, ChronoUnit.SECONDS)) / 3600.0));

            // if start and end are on the same business day, the result is the difference
            //
            if (zoned_started.getDayOfYear() == zoned_ended.getDayOfYear()) {
                logger.debug("same business days - duration -> "
                        + Double.valueOf((zonedDateTimeDifference(zoned_started, zoned_ended, ChronoUnit.SECONDS)) / 3600.0));
                return Double.valueOf((zonedDateTimeDifference(zoned_started, zoned_ended, ChronoUnit.SECONDS)) / 3600.0);
            }

            hours = Double
                    .valueOf(
                            (zonedDateTimeDifference(zoned_started,
                                    zoned_started.truncatedTo(ChronoUnit.DAYS)
                                            .plusHours(Double.valueOf(getEndWorkableDay()).intValue()),
                                    ChronoUnit.SECONDS)) / 3600.0);
            logger.debug("Hours  -> " + hours);

            ZonedDateTime zoned_walker = zoned_started.truncatedTo(ChronoUnit.DAYS).plusDays(1);

            boolean done = false;

            while (!done) {

                if (isNonWorkableDay(zoned_walker) || isWeekEnd(zoned_walker)) {
                    logger.debug("NonWorkableDay -> " + ServiceLocator.getService(DateTimeService.class).format(
                            zoned_walker.withZoneSameInstant(ZoneId.of(zid)).toOffsetDateTime(), tz, lo,
                            DateTimeService.Dow_Month_Day_Year_hh_mm));
                    zoned_walker = zoned_walker.truncatedTo(ChronoUnit.DAYS).plusDays(1);
                } else {

                    // end of day
                    ZonedDateTime eod = zoned_walker.plusHours(Double.valueOf(getEndWorkableDay()).intValue());

                    if (eod.isAfter(zoned_ended)) {
                        // si termina antes del fin del dia
                        logger.debug("Last Business Day -> " + ServiceLocator.getService(DateTimeService.class).format(
                                eod.withZoneSameInstant(ZoneId.of(zid)).toOffsetDateTime(), tz, lo,
                                DateTimeService.Dow_Month_Day_Year_hh_mm));

                        ZonedDateTime sod = zoned_walker.truncatedTo(ChronoUnit.DAYS)
                                .plusHours(Double.valueOf(getStartWorkableDay()).intValue());

                        logger.debug("Adding delta -> "
                                + Double.valueOf((zonedDateTimeDifference(sod, zoned_ended, ChronoUnit.SECONDS)) / 3600.0));
                        hours = hours + Double.valueOf((zonedDateTimeDifference(sod, zoned_ended, ChronoUnit.SECONDS)) / 3600.0);
                        done = true;
                    } else {
                        // si termina despues del fin del dia laborable, suma todo el dia laborable
                        logger.debug("Not Last Business Day -> " + ServiceLocator.getService(DateTimeService.class).format(
                                eod.withZoneSameInstant(ZoneId.of(zid)).toOffsetDateTime(), tz, lo,
                                DateTimeService.Dow_Month_Day_Year_hh_mm));
                        logger.debug("Adding Whole day -> " + (getEndWorkableDay() - getStartWorkableDay()));

                        hours = hours + (getEndWorkableDay() - getStartWorkableDay());
                        zoned_walker = zoned_walker.truncatedTo(ChronoUnit.DAYS).plusDays(1);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }

        logger.debug("Total hours -> " + hours);

        return hours;

    }

    /**
     * @param date
     * @return
     */
    private boolean isWorkableZonedDateTime(ZonedDateTime date) {

        if (isNonWorkableDay(date))
            return false;

        if (isWeekEnd(date))
            return false;

        ZonedDateTime start = date.truncatedTo(ChronoUnit.DAYS).plusHours(Double.valueOf(getStartWorkableDay()).intValue());
        ZonedDateTime end = date.truncatedTo(ChronoUnit.DAYS).plusHours(Double.valueOf(getEndWorkableDay()).intValue());

        if (date.isBefore(start))
            return false;

        if (date.isAfter(end))
            return false;

        return true;

    }

    /**
     * @param date
     * @return
     */
    private ZonedDateTime getNextWorkableZonedDateTime(ZonedDateTime date) {

        if (isWorkableZonedDateTime(date))
            return date;

        if (!isNonWorkableDay(date) && !isWeekEnd(date)) {

            ZonedDateTime start = date.truncatedTo(ChronoUnit.DAYS).plusHours(Double.valueOf(getStartWorkableDay()).intValue());

            if (date.isBefore(start))
                return start;

            ZonedDateTime end = date.truncatedTo(ChronoUnit.DAYS).plusHours(Double.valueOf(getEndWorkableDay()).intValue());

            if (date.isBefore(end))
                return date;
        }

        ZonedDateTime candidate = date.truncatedTo(ChronoUnit.DAYS).plusDays(1).truncatedTo(ChronoUnit.DAYS);

        while (isNonWorkableDay(candidate) || isWeekEnd(candidate))
            candidate = candidate.truncatedTo(ChronoUnit.DAYS).plusDays(1);

        return candidate.truncatedTo(ChronoUnit.DAYS).plusHours(Double.valueOf(getStartWorkableDay()).intValue());
    }

    /**
     * 
     * @return
     */
    public double getStartWorkableDay() {
        if (START_HOUR_WORKABLE_DAY == null) {
            String ct = getContentDao().findSystemParameterValueByKey("workflow.compliance.starthour.workable.day", "8");
            try {
                START_HOUR_WORKABLE_DAY = Double.valueOf(ct.trim());
            } catch (Exception e) {
                logger.error(e);
                START_HOUR_WORKABLE_DAY = Double.valueOf(8);
            }
            logger.debug("START_HOUR_WORKABLE_DAY -> " + START_HOUR_WORKABLE_DAY.intValue());
        }
        return START_HOUR_WORKABLE_DAY.doubleValue();
    }

    public double getEndWorkableDay() {
        if (END_HOUR_WORKABLE_DAY == null) {
            String ct = getContentDao().findSystemParameterValueByKey("workflow.compliance.endhour.workable.day", "17");
            try {
                END_HOUR_WORKABLE_DAY = Double.valueOf(ct.trim());
            } catch (Exception e) {
                logger.error(e);
                END_HOUR_WORKABLE_DAY = Double.valueOf(17);
            }
            logger.debug("END_HOUR_WORKABLE_DAY -> " + END_HOUR_WORKABLE_DAY.intValue());
        }
        return END_HOUR_WORKABLE_DAY.doubleValue();
    }

    public int getCutoffTime() {
        if (CUTOFF_TIME == null) {
            String ct = getContentDao().findSystemParameterValueByKey("workflow.compliance.cutoff.time", "17");
            try {
                CUTOFF_TIME = Integer.valueOf(ct.trim());
            } catch (Exception e) {
                logger.error(e);
                CUTOFF_TIME = Integer.valueOf(17);
            }
            logger.debug("CUTOFF_TIME -> " + CUTOFF_TIME.intValue());
        }
        return CUTOFF_TIME.intValue();
    }

    /**
     * days: the number of workable days allowed to the task.
     * 
     * The Due Date is calculated according to the Domain Time Zone. If the time it
     * started is before 12 mid day in the Domain Time Zone, then the first day is
     * the current day. if not, the first day is the next day.
     * 
     * 
     * 
     * 
     */
    public OffsetDateTime getDueDate(OffsetDateTime started, int days) {

        if (getDomain() == null)
            throw new IllegalArgumentException("Domain is null");

        boolean done = false;

        // -------------------------------------------------------
        // if it is past 12 hs -> current day counts
        // if it is before 12 hs -> current day does not count
        // -------------------------------------------------------
        //
        // By convention. if the submission was before 12 midday -> current day counts
        // if not. next day is the first to count
        //
        // First full day Assigned

        DateTimeService service = ServiceLocator.getService(DateTimeService.class);
        String zid = null;

        logger.debug("Started -> " + ServiceLocator.getService(DateTimeService.class).getDateDisplayString(started) + "  | days -> "
                + String.valueOf(days));

        if (getDomain().getTimeZone() != null)
            zid = service.getMapZoneIds().get(getDomain().getTimeZone());

        if (zid == null)
            zid = ZoneId.systemDefault().getId();

        ZonedDateTime zoned_started = ZonedDateTime.ofInstant(started.toInstant(), ZoneId.of(zid));
        ZonedDateTime zoned_walker;

        if (zoned_started.getHour() <= getCutoffTime()) {
            logger.debug("Hour is: " + String.valueOf(zoned_started.getHour()) + "(before cutoff time "
                    + String.valueOf(getCutoffTime()));
            zoned_walker = zoned_started.truncatedTo(ChronoUnit.DAYS).plusHours(8);
        } else {
            logger.debug("Hour is: " + String.valueOf(zoned_started.getHour()) + "(after cutoff time "
                    + String.valueOf(getCutoffTime()));
            zoned_walker = zoned_started.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(8);
        }

        // initial candidate, the actual
        // due date will be this day or later
        //
        int n = days + 1;

        DateTimeFormatter df;

        df = DateTimeFormatter.ofPattern("EEE dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        logger.debug("Start -> " + df.format(zoned_started) + " days -> " + String.valueOf(days));
        logger.debug("Intial Candidate -> " + df.format(zoned_walker));

        while (!done) {

            if (!isWeekEnd(zoned_walker) && !isNonWorkableDay(zoned_walker)) {
                logger.debug(" is Workable -> " + df.format(zoned_walker));
                n = n - 1;
            } else
                logger.debug(df.format(zoned_walker) + "is Non Workable -> " + (isWeekEnd(zoned_walker) ? " week end" : "holyday"));

            if (n == 0)
                done = true;
            else
                zoned_walker = zoned_walker.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(8);

            if (n > 200) {
                logger.error("n > 200 please check the algorithm.");
                throw new KbeeRuntimeException("n > 200 please check the algorithm.");
            }
        }

        logger.debug("Due Date -> " + df.format(zoned_walker));

        return zoned_walker.toOffsetDateTime();

    }

    public OffsetDateTime getDueDate(int days) {
        return (getDueDate(OffsetDateTime.now(), days));
    }

    public Domain getDomain() {
        return domain;
    }

    public void evict() {
        synchronized (this) {
            dm_nw.clear();
            dm_nw_map.clear();
            CUTOFF_TIME = null;
        }
    }

    @Override
    public boolean listen(Event event) {
        if (event instanceof com.novamens.kbee.event.EvictCacheServiceEvent)
            return true;
        return false;
    }

    @Override
    public void onEvent(Event event) {
        logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
        if (event instanceof com.novamens.kbee.event.EvictCacheServiceEvent)
            evict();
    }

    /**
     * Generates cache for Domain
     * 
     */
    private synchronized void generate() {

        String key = getDomain().getId().toString();

        List<LocalDate> nw = new ArrayList<LocalDate>();
        Map<String, LocalDate> nw_map = new HashMap<String, LocalDate>();

        int current_year = LocalDate.now().getYear();

        List<String> days = new ArrayList<String>();

        String nonw = getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.CALENDAR_NON_WORKABLE_DAYS);

        if (nonw == null)
            nonw = getContentDao().findSystemParameterValueByKey("nonworkabledays", default_non_workable);

        String arr[] = nonw.split(";");

        for (String a : arr)
            days.add(a.trim());

        for (String str : days) {
            String row[] = str.split("[-|#]");
            if (row[0] != null) {
                String date = row[0].trim();
                String elem[] = date.split("/");
                int day, month, year = -1;
                try {
                    if (elem.length > 0) {

                        month = (Integer.valueOf(elem[0].trim())).intValue();

                        if (elem.length > 1) {
                            day = (Integer.valueOf(elem[1].trim())).intValue();

                            if (elem.length > 2) {
                                year = (Integer.valueOf(elem[2].trim())).intValue();
                            }

                            if (year > 0) {
                                LocalDate ld = LocalDate.of(year, month, day);
                                nw.add(ld);
                            } else {
                                for (int n = current_year; (n < current_year + 10); n++) {
                                    LocalDate ld = LocalDate.of(n, month, day);
                                    nw.add(ld);
                                }
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    logger.error(e);
                }
            }
        }
        for (LocalDate c : nw)
            nw_map.put(String.valueOf(c.getYear()) + String.valueOf(c.getDayOfYear()), c);

        Collections.sort(nw);

        if (logger.isDebugEnabled())
            nw.forEach(item -> logger.debug(item.toString()));

        dm_nw.put(key, nw);
        dm_nw_map.put(key, nw_map);
        last_generated.put(key, Instant.now());
    }

    private boolean isWeekEnd(ZonedDateTime day) {
        return (day.getDayOfWeek() == DayOfWeek.SATURDAY) || (day.getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    private boolean isNonWorkableDay(ZonedDateTime day) {
        String key = String.valueOf(day.getYear()) + String.valueOf(day.getDayOfYear());
        if (getNonWorkableMap().containsKey(key))
            return true;
        return false;
    }

    /**
     * @param day
     * @return private boolean isWorkableHour(ZonedDateTime day) { if
     *         (isNonWorkableDay(day) || isWeekEnd(day)) return false; return true;
     *         }
     */

    /**
     * @return
     */
    private Map<String, LocalDate> getNonWorkableMap() {
        String key = getDomain().getId().toString();
        if ((dm_nw_map.get(key) == null) | (last_generated.get(key) == null)
                || (Instant.now().isAfter(last_generated.get(key).plusSeconds(CACHE_DURATION)))) {
            logger.debug("cache expired for domain id: " + key);
            generate();
        }
        return dm_nw_map.get(key);
    }

    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

}
