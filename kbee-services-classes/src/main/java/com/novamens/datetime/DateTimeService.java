package com.novamens.datetime;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.Temporal;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;

import kbee.util.DateFormatterLabels;

public class DateTimeService implements SystemService {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DateTimeService.class.getName());

    static final public DateTimeFormatter full_eng = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z uuuu", Locale.ENGLISH);
    static final public DateTimeFormatter full_spa = DateTimeFormatter.ofPattern("EEE dd MMM HH:mm:ss z uuuu", Locale.forLanguageTag("es"));

    static final int LOCAL_TSTAMP_LENGTH = ("yyyy-MM-dd HH:mm:ss").length();

    static final DateTimeFormatter local_tstamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final public DateTimeFormatter postgres_df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    static final public DateTimeFormatter hibernate = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.XXX-z", Locale.ENGLISH);
    static final public DateTimeFormatter database_timestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    static DateTimeFormatter solr_field = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    static final Locale LOCALE_ES = new Locale("es");

    static final private int DATE_LEN = "yyyy-mm-dd".length();

    static final public DateTimeFormatter legacy_offsetdatedatetime_iso_offset_date_time = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
    static final public SimpleDateFormat legacy_date_iso_offset_date_time_x = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",
            Locale.getDefault());

    static private final SimpleDateFormat legacy_date_colloquial_displayformat_en = new SimpleDateFormat("MMM d yyyy");
    static private final SimpleDateFormat legacy_date_number_displayformat_en = new SimpleDateFormat("MM/dd/yyyy");

    static private final SimpleDateFormat legacy_date_colloquial_displayformat_es = new SimpleDateFormat("d MMM yyyy");
    static private final SimpleDateFormat legacy_date_number_displayformat_es = new SimpleDateFormat("d/MM/yyyy");

    static public int DATE_MONTH_COLLOQUIAL_FORMAT = 100;
    static public int DATE_FORMAT = 200;
    static public int DATE_FORMAT_GMT = 300;

    static long KB = 1024;
    static long MB = 1000 * KB;
    static long GB = 1000 * MB;

    public static final String ONLY_AGO_LABEL = "AGO";
    public static final String COLlOQUIAL_AGO_LABEL = "COLLOQUIAL_AGO";
    public static final String COLlOQUIAL_LABEL = "COLLOQUIAL";
    public static final String MONTH_DAY_YEAR_LABEL = "DATE";
    public static final String MONTH_DAY_YEAR_GMT_LABEL = "DATE_GMT";
    public static final String FULL_LABEL = "FULL";
    public static final String TIMESTAMP_LABEL = "TIMESTAMP";

    public static final int ONLY_AGO = 1;
    public static final int DATE_COLlOQUIAL = 2;
    public static final int DATE_COLlOQUIAL_AGO = 3;

    public final static int Full = 0;
    public final static int Month_Day_Year_hh_mm_ss_zzz = 1;
    public final static int Month_Day_Year_hh_mm = 2;
    public final static int Month_Day_Year = 3;

    public final static int Month_Day_hr_min = 4;
    public final static int Dateformat_short_this_year = 5;
    public final static int Hour_of_today_format = 6;
    public final static int Hour_of_day_this_week_format = 7;
    public final static int Am_pm_format = 8;
    public final static int Day_Month_Year_hh_mm_ss_zzz = 9;
    public final static int Day_Month_Year_hh_mm_ss = 10;

    public final static int Dow_Month_Day_Year_hh_mm = 11;

    public final static int Year_Month_Day = 12;
    public final static int Dow_Month_Day_year_z = 13;

    public final static int Hibernate = 14;

    public final static int Full_GMT = 15;

    public final static int Dow_Month_Day_Year_hh_mm_z = 16;
    public final static int Month_Day_Year_gmt = 17;

    public final static int Dow_Month_Day_year = 18;

    static public DateTimeFormatter getDefaultDateTime_Date_Formatter() {
        if (Locale.getDefault().getLanguage().equals("es"))
            return formatter_spa[Month_Day_Year];
        else
            return formatter_eng[Month_Day_Year];

    }

    static public DateTimeFormatter getDefaultDateTime_Time_Formatter() {
        if (Locale.getDefault().getLanguage().equals("es"))
            return formatter_spa[Month_Day_Year_hh_mm];
        else
            return formatter_eng[Month_Day_Year_hh_mm];
    }

    /**
     * Default Date : January 23, 2018 Default DateTime : January 23, 2018 10:12 am
     */
    static long dateTimeDifference(Temporal d1, Temporal d2, ChronoUnit unit) {
        return unit.between(d1, d2);
    }

    public String getSolrFieldValue(OffsetDateTime date) {
        return solr_field.format(date);
    }

    static DateTimeFormatter formatter_eng[] = { DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z uuuu", Locale.ENGLISH), // 0 for
                                                                                                                            // Time
            DateTimeFormatter.ofPattern("MMM d yyyy HH:mm:ss zz", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d yyyy h:mm", Locale.ENGLISH), // Default agrega am pm aparte.
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH), // month day year 3
            DateTimeFormatter.ofPattern("MMM d, h:mm", Locale.ENGLISH), DateTimeFormatter.ofPattern("MMM d, h:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH), DateTimeFormatter.ofPattern("EEEE h:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern(" a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss  zzz", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss ", Locale.ENGLISH), // 10
            DateTimeFormatter.ofPattern("EEE MMM d yyyy hh:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy MM d HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEE MMM d yyyy z", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.XXX-z", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss z", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEE MMM d yyyy hh:mm z", Locale.ENGLISH), // 16
            DateTimeFormatter.ofPattern("MMM d yyyy x", Locale.ENGLISH), // 17 month day year gmt
            DateTimeFormatter.ofPattern("EEE MMM d yyyy", Locale.ENGLISH)

    };

    static DateTimeFormatter formatter_spa[] = { DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z uuuu", LOCALE_ES), // · HH:mm:ss
                                                                                                                       // z uuuu
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss zzz", LOCALE_ES),
            DateTimeFormatter.ofPattern("d MMM yyyy h:mm", LOCALE_ES), DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE_ES),
            DateTimeFormatter.ofPattern("d MMM h:mm", LOCALE_ES), DateTimeFormatter.ofPattern("d MMM, h:mm", LOCALE_ES),
            DateTimeFormatter.ofPattern("h:mm", LOCALE_ES), DateTimeFormatter.ofPattern("EEEE h:mm", LOCALE_ES),
            DateTimeFormatter.ofPattern(" a", LOCALE_ES), // 8
            DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss  zzz", LOCALE_ES),
            DateTimeFormatter.ofPattern("d MMM yyyy - HH:mm:ss ", LOCALE_ES), // 10 ·
            DateTimeFormatter.ofPattern("EEE MMM d yyyy hh:mm", LOCALE_ES),
            DateTimeFormatter.ofPattern("yyyy MM d HH:mm:ss", LOCALE_ES),
            DateTimeFormatter.ofPattern("EEE d MMM yyyy z", LOCALE_ES),
            DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.XXX-z", LOCALE_ES),
            DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss z", LOCALE_ES),
            DateTimeFormatter.ofPattern("EEE d MMM yyyy hh:mm z", LOCALE_ES),
            DateTimeFormatter.ofPattern("d MMM yyyy x", LOCALE_ES), // 17 month day year gmt
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", LOCALE_ES) };

    final static private long MINUTOS = 3600000L;
    final static private long HORAS = 86400000 * 1L;
    final static private long DIAS = 86400000 * 90L;
    final static private long SEGUNDOS = 120000L;

    static private final String second_eng = "second";
    static private final String seconds_eng = "seconds";

    static private final String minute_eng = "minute";
    static private final String minutes_eng = "minutes";

    static private final String hour_eng = "hour";
    static private final String hours_eng = "hours";

    static private final String day_eng = "day";
    static private final String days_eng = "days";

    static private final String second_spa = "segundo";
    static private final String seconds_spa = "segundos";

    static private final String minute_spa = "minuto";
    static private final String minutes_spa = "minutos";

    static private final String hour_spa = "hora";
    static private final String hours_spa = "horas";

    static private final String day_spa = "día";
    static private final String days_spa = "días";

    static private final String prefix_eng = "";
    static private final String prefix_spa = "hace ";

    static private final String suffix_eng = " ago";
    static private final String suffix_spa = "";

    static public final int ENG = 0;
    static public final int SPA = 1;

    static private DateFormatterLabels LABELS[];

    static {
        LABELS = new DateFormatterLabels[2];

        LABELS[ENG] = new DateFormatterLabels();
        LABELS[SPA] = new DateFormatterLabels();

        LABELS[ENG].second = second_eng;
        LABELS[ENG].seconds = seconds_eng;
        LABELS[ENG].minute = minute_eng;
        LABELS[ENG].minutes = minutes_eng;
        LABELS[ENG].hours = hours_eng;
        LABELS[ENG].hour = hour_eng;
        LABELS[ENG].day = day_eng;
        LABELS[ENG].days = days_eng;
        LABELS[ENG].prefix = prefix_eng;
        LABELS[ENG].suffix = suffix_eng;

        LABELS[SPA].second = second_spa;
        LABELS[SPA].seconds = seconds_spa;

        LABELS[SPA].minute = minute_spa;
        LABELS[SPA].minutes = minutes_spa;
        LABELS[SPA].hours = hours_spa;
        LABELS[SPA].hour = hour_spa;
        LABELS[SPA].prefix = prefix_spa;
        LABELS[SPA].day = day_spa;
        LABELS[SPA].days = days_spa;
        LABELS[SPA].suffix = suffix_spa;
    }

    Map<String, String> ordered_zones = null;
    Map<String, String> map_zones = null;

    public String getDefaultScritpDateMask(String label, Locale locale) {
        String ma = getDefaultDateMask(locale);
        return "${" + label + " ?string[\"" + ma + "\"]} ";
    }

    public String getDefaultDateMask(Locale locale) {
        if (locale.getLanguage().equals("es") || locale.getLanguage().equals("spa"))
            return "dd/MM/yy";
        else
            return "MM/dd/yy";
    }

    public LocalDateTime millsToLocalDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Default Locale, Time Zone, and css "ago"
     */
    public String timeElapsed(OffsetDateTime date) {
        return timeElapsed(date, null, null);
    }

    public String formatTZ(OffsetDateTime date, String timezone, Locale locale, int formatter) {

        ZoneId zid = ZoneId.of(timezone);

        if (zid == null) {
            zid = getDefaultZoneId();
        }

        if (locale == null) {
            locale = getDefaultLocale();
        }

        return format(date, zid.getId(), locale, formatter);
    }

    public String format(OffsetDateTime date) {
        return format(date, null, null, Day_Month_Year_hh_mm_ss_zzz);
    }

    /**
     * 
     * @param date
     * @param zid
     * @param locale
     * @param formatter
     * @return
     */
    public String format(OffsetDateTime date, String zid, Locale locale, int formatter) {

        if (date == null)
            return "err";

        ZoneId zone = null;

        if (zid == null) {
            zone = getDefaultZoneId();
        } else
            zone = ZoneId.of(zid);

        if (locale == null) {
            locale = getDefaultLocale();
        }

        ZonedDateTime zdate = ZonedDateTime.ofInstant(date.toInstant(), zone);

        if (locale == Locale.ENGLISH) {
            if (formatter < 0 || formatter > (formatter_eng.length - 1))
                return full_eng.format(zdate);

            if (formatter == Month_Day_Year_hh_mm)
                return formatter_eng[formatter].format(zdate) + formatter_eng[Am_pm_format].format(date).toLowerCase();

            else if (formatter == Dow_Month_Day_Year_hh_mm)
                return formatter_eng[formatter].format(zdate) + formatter_eng[Am_pm_format].format(date).toLowerCase();

            else if (formatter == Dow_Month_Day_year_z)
                return formatter_eng[formatter].format(zdate);

            else
                return formatter_eng[formatter].format(zdate);

        } else {
            if (formatter < 0 || formatter > (formatter_spa.length - 1))
                return full_spa.format(zdate);

            if (formatter == Month_Day_Year_hh_mm)
                return formatter_spa[formatter].format(zdate) + formatter_spa[Am_pm_format].format(date).toLowerCase();

            else if (formatter == Dow_Month_Day_Year_hh_mm)
                return formatter_spa[formatter].format(zdate) + formatter_spa[Am_pm_format].format(date).toLowerCase();

            else if (formatter == Dow_Month_Day_year_z)
                return formatter_spa[formatter].format(zdate);

            else
                return formatter_spa[formatter].format(zdate);
        }
    }

    public String timeElapsed(LocalDateTime date, Locale locale) {
        return timeElapsed(date, locale);
    }

    /**
     * if {@code zid} is null, the JVM Time Zone will be used if {@code  locale} is
     * null, the JVM default Locale will be used
     */
    public String timeElapsed(LocalDateTime date, Locale locale, String css) {

        if (date == null)
            return "err";

        ZoneId zone = getDefaultZoneId();
        ZonedDateTime zdate = ZonedDateTime.ofInstant(date.toInstant(OffsetDateTime.now().getOffset()), zone);
        return timeElapsed(zdate, zone, locale, DATE_COLlOQUIAL_AGO, css);
    }

    /**
     * if {@code zid} is null, the JVM Time Zone will be used if {@code  locale} is
     * null, the JVM default Locale will be used
     */
    public String timeElapsed(OffsetDateTime date, String zid, Locale locale) {

        if (date == null)
            return "err";

        ZoneId zone = null;

        if (zid == null) {
            zone = getDefaultZoneId();
        } else
            zone = ZoneId.of(zid);

        if (locale == null) {
            locale = getDefaultLocale();
        }

        ZonedDateTime zdate = ZonedDateTime.ofInstant(date.toInstant(), zone);
        return timeElapsed(zdate, zone, locale, DATE_COLlOQUIAL_AGO, "ago");
    }

    /**
     *
     */
    public String getLocalDateTime(ZonedDateTime date, String tmz, Locale locale, int mask) {
        return getDateTimeFormater(locale)[mask].format(date.withZoneSameInstant(ZoneId.of(tmz)));
    }

    /**
     * ZoneId tmz Locale locale
     */
    public String timeElapsed(OffsetDateTime xdate, ZoneId tmz, Locale locale, int mode, String css_ago) {
        return timeElapsed(ZonedDateTime.ofInstant(xdate.toInstant(), tmz), tmz, locale, mode, css_ago);
    }

    /**
     *
     */
    public String timeElapsed(ZonedDateTime xdate, ZoneId tmz, Locale locale, int mode, String css_ago) {

        if (xdate == null)
            return null;

        String label;

        ZonedDateTime today = ZonedDateTime.now(tmz);
        ZonedDateTime date = xdate.withZoneSameInstant(tmz);

        if (date == null)
            return null;

        if (locale == null) {
            locale = getDefaultLocale();
        }

        if (xdate.isAfter(today))
            return getDateTimeFormater(locale)[Month_Day_Year].format(date);

        String minute, minutes, hour, hours, prefix, suffix, day, days;

        int lang = locale.getLanguage().equals(Locale.ENGLISH.getLanguage()) ? ENG : SPA;

        if (lang == ENG) {
            minute = minute_eng;
            minutes = minutes_eng;
            hours = hours_eng;
            hour = hour_eng;
            day = day_eng;
            days = days_eng;
            prefix = prefix_eng;
            suffix = suffix_eng;

        } else {
            minute = minute_spa;
            minutes = minutes_spa;
            hours = hours_spa;
            hour = hour_spa;
            prefix = prefix_spa;
            suffix = suffix_spa;
            day = day_spa;
            days = days_spa;
        }

        long diff = dateTimeDifference(date, today, ChronoUnit.MILLIS);

        DateTimeFormatter default_formatter = getDateTimeFormater(locale)[Month_Day_Year_hh_mm];
        DateTimeFormatter hour_of_today_format = getDateTimeFormater(locale)[Hour_of_today_format];
        DateTimeFormatter am_pm_format = getDateTimeFormater(locale)[Am_pm_format];
        DateTimeFormatter hour_of_day_this_week_format = getDateTimeFormater(locale)[Hour_of_day_this_week_format];
        DateTimeFormatter dateformat_short_this_year = getDateTimeFormater(locale)[Dateformat_short_this_year];

        // minutos
        //
        // ONLY_AGO: hace nn minutos
        // DATE_COLLQUIAL_AGO: 12:43 am (hace 3 minutos)
        // DATE_COLLQUIAL: 12:43 am

        if (diff < MINUTOS) {
            long minutos = diff / 60000;
            String xago = prefix + String.valueOf(minutos) + ((minutos == 1) ? " " + minute : " " + minutes) + suffix;
            if (mode == ONLY_AGO)
                label = xago;
            else {
                if (mode == DATE_COLlOQUIAL_AGO) {
                    String strzago = (css_ago != null ? "<span class=\"" + css_ago + "\">" : "") + " (" + xago + ")"
                            + (css_ago != null ? " </span>" : "");
                    label = hour_of_today_format.format(date) + am_pm_format.format(date).toLowerCase() + strzago;
                } else {
                    label = hour_of_today_format.format(date) + am_pm_format.format(date).toLowerCase();
                }
            }
        }
        // horas
        //
        // ONLY_AGO: hace 3 horas
        // DATE_COLLQUIAL_AGO: 12:43 am (hace 3 horas)
        //
        else if (diff < HORAS) {
            long horas = diff / 3600000;
            String xago = prefix + String.valueOf(horas) + ((horas == 1) ? " " + hour : " " + hours) + suffix;
            if (mode == ONLY_AGO)
                label = xago;
            else {
                if (mode == DATE_COLlOQUIAL_AGO) {
                    String strzago = (css_ago != null ? "<span class=\"" + css_ago + "\">" : "") + " (" + xago + ")"
                            + (css_ago != null ? " </span>" : "");
                    label = hour_of_today_format.format(date) + am_pm_format.format(date).toLowerCase() + strzago;
                    ;
                } else
                    label = hour_of_today_format.format(date) + am_pm_format.format(date).toLowerCase();
            }

        }
        // ultimos 90 dias
        // ONLY_AGO: hace 3 dias
        // DATE_COLLQUIAL_AGO: Nov 12, 12:43 am (hace 3 dias)
        //
        else if (diff < DIAS) {
            long dias = diff / 86400000;
            String xago = prefix + String.valueOf(dias) + ((dias == 1) ? " " + day : " " + days) + suffix;
            if (mode == ONLY_AGO)
                label = xago;
            else {
                // Si es la misma semana: Miercoles 3.23 pm
                //
                if (isSameWeek(today, date)) {
                    if (mode == DATE_COLlOQUIAL_AGO) {
                        String strzago = (css_ago != null ? "<span class=\"" + css_ago + "\">" : "") + " (" + xago + ")"
                                + (css_ago != null ? " </span>" : "");
                        label = hour_of_day_this_week_format.format(date) + am_pm_format.format(date).toLowerCase() + strzago;
                    } else {
                        label = hour_of_day_this_week_format.format(date) + am_pm_format.format(date).toLowerCase();
                    }
                }
                // Sino Nov 15, 3.23 pm
                else if (isSameYear(today, date)) {
                    if (mode == DATE_COLlOQUIAL_AGO) {
                        String strzago = (css_ago != null ? "<span class=\"" + css_ago + "\">" : "") + " (" + xago + ")"
                                + (css_ago != null ? " </span>" : "");
                        label = dateformat_short_this_year.format(date) + am_pm_format.format(date).toLowerCase() + strzago;
                    } else {
                        label = dateformat_short_this_year.format(date) + am_pm_format.format(date).toLowerCase();
                    }
                } else {
                    if (mode == DATE_COLlOQUIAL_AGO) {
                        String strzago = (css_ago != null ? "<span class=\"" + css_ago + "\">" : "") + " (" + xago + ")"
                                + (css_ago != null ? " </span>" : "");
                        label = default_formatter.format(date) + am_pm_format.format(date).toLowerCase() + strzago;
                    } else
                        label = default_formatter.format(date) + am_pm_format.format(date).toLowerCase();
                }
            }
        }

        //
        // Más de 90 dias
        // coloquial va sin ago
        //
        else {
            if (isSameYear(today, date)) {
                if (mode == ONLY_AGO) {
                    long dias = diff / 86400000;
                    label = prefix + String.valueOf(dias) + ((dias == 1) ? " " + day : " " + days) + suffix;
                } else
                    label = dateformat_short_this_year.format(date) + am_pm_format.format(date).toLowerCase();
            } else {
                if (mode == ONLY_AGO) {
                    long dias = diff / 86400000;
                    label = prefix + String.valueOf(dias) + ((dias == 1) ? " " + day : " " + days) + suffix;
                } else
                    label = default_formatter.format(date) + am_pm_format.format(date).toLowerCase();
                ;
            }
        }
        return label;
    }

    /**
     * For durations that are measured in seconds.
     *
     * @param sg
     * @param lang
     * @return
     */

    public String formatLapseSeconds(long sg, Locale locale) {
        return formatLapseSeconds(sg, locale, null);
    }

    /**
     *
     */
    public boolean isSameYear(ZonedDateTime today, ZonedDateTime date) {
        int today_year = today.getYear();
        int date_year = date.getYear();
        return (today_year == date_year);
    }

    /**
     *
     */
    public boolean isSameWeek(ZonedDateTime today, ZonedDateTime date) {
        int week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekYear = today.get(IsoFields.WEEK_BASED_YEAR);
        int date_week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int date_weekYear = date.get(IsoFields.WEEK_BASED_YEAR);
        return week == date_week && weekYear == date_weekYear;
    }

    /**
     * @param sg
     * @param lang
     * @param css
     * @return
     */

    public String formatLapseSeconds(long sg, Locale locale, String css) {

        DateFormatterLabels df;

        if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage()))
            df = LABELS[ENG];
        else
            df = LABELS[SPA];

        String label;

        String prefix = css != null ? "<span class= \"" + css + "\">" : "";
        String suffix = css != null ? "</span>" : "";

        if (sg < 1000) {
            label = String.valueOf(sg) + (sg > 0 ? (prefix + " miliseconds" + suffix) : "");
        } else if (sg < SEGUNDOS) {
            long segs = sg / 1000;
            label = String.format("%d.2", segs)
                    + ((segs == 1) ? (" " + prefix + df.second + suffix) : (" " + prefix + df.seconds + suffix));
        } else if (sg < MINUTOS) {
            long minutos = sg / 60000;
            if (minutos == 1)
                label = "1 " + df.minute;
            else
                label = String.format("%d.2", minutos)
                        + ((minutos == 1) ? (" " + prefix + df.minute + suffix) : (" " + prefix + df.minutes + suffix));
        } else if (sg < HORAS) {
            long horas = sg / 3600000;
            label = df.prefix + String.format("%d.2", horas)
                    + ((horas == 1) ? (" " + prefix + df.hour + suffix) : (" " + prefix + df.hours + suffix));
        } else if (sg < DIAS) {
            long dias = sg / 86400000;
            if (dias < 7) {
                label = String.format("%d.2", dias)
                        + ((dias == 1) ? (" " + prefix + df.day + suffix) : (" " + prefix + df.days + suffix));
            } else
                label = String.format("%d.2", dias)
                        + ((dias == 1) ? " " + (" " + prefix + df.day + suffix) : (" " + prefix + df.days + suffix));
        } else {
            label = String.valueOf(sg) + (prefix + " miliseconds" + suffix);
        }
        return label;
    }

    private DateTimeFormatter[] getDateTimeFormater(Locale locale) {
        if (locale.getLanguage() == Locale.ENGLISH.getLanguage())
            return formatter_eng;
        else
            return formatter_spa;
    }

    /**
     * key: US/Central value: -05:00
     */
    public Map<String, String> getMapZoneIds() {
        if (map_zones == null)
            getOrderedZoneIds();
        return map_zones;
    }

    /**
     *
     */
    public Map<String, String> getMapZoneValues() {
        if (map_zones == null)
            getOrderedZoneIds();
        return map_zones;
    }

    public OffsetDateTime getOffsetDateTime(long longValue, User user) {
        String zid = getMapZoneIds().get(user.getTimeZone());
        return getOffsetDateTime(longValue, (zid != null) ? ZoneId.of(zid) : ZoneId.systemDefault());
    }

    public OffsetDateTime getOffsetDateTime(long longValue, ZoneId tmz) {
        LocalDateTime date = Instant.ofEpochMilli(longValue).atZone(tmz).toLocalDateTime();
        OffsetDateTime odate = OffsetDateTime.of(date, OffsetDateTime.now().getOffset());
        return odate;
    }

    public OffsetDateTime getOffsetDateTime(long longValue) {
        ZoneId tmz = ZoneId.systemDefault();
        return getOffsetDateTime(longValue, tmz);
    }

    /****
     * 
     * 1. Para obtener un String para guardar en la base JSON
     * ------------------------------------------------------ public String
     * getStr_ISO_OFFSET_DATE_TIME(Date date) public String
     * getStr_ISO_OFFSET_DATE_TIME(OffsetDateTime date)
     * 
     * 
     * 2. Para obtener el Date desde el String de la base
     * ----------------------------------------------- public OffsetDateTime
     * parseStrDate(String str_date)
     * 
     * NOTA: No soportamos devolver Date
     * 
     * 
     * 3. Para obtener un String para Display en la UI
     * -----------------------------------------------
     * 
     * public String getDateDisplayString(OffsetDateTime date) public String
     * getDateDisplayString(OffsetDateTime date, Locale locale) public String
     * getDateDisplayString(OffsetDateTime date, Locale locale, int mode)
     * 
     * public String getDateDisplayString(Date date) public String
     * getDateDisplayString(Date date, Locale locale) public String
     * getDateDisplayString(Date date, Locale locale, int mode)
     * 
     * mode: DateTimeService.DATE_MONTH_COLLOQUIAL_FORMAT
     * DateTimeService.DATE_FORMAT
     * 
     * 
     * 4. Si necesitas un Date del OffsetDateTime (luego de 2. por ej)
     * ------------------------------------------ -------------------- Date date =
     * new Date(odate.toInstant().toEpochMilli());
     ****/

    // 1
    public String getDateDisplayString(OffsetDateTime date) {

        Locale locale = getDefaultLocale();

        return getDateDisplayString(date, locale, DATE_MONTH_COLLOQUIAL_FORMAT);
    }

    // 2
    public String getDateDisplayString(OffsetDateTime date, Locale locale) {
        return getDateDisplayString(date, locale, DATE_MONTH_COLLOQUIAL_FORMAT);
    }

    // 3
    public String getDateDisplayString(String str, Locale locale, int mode) {
        return (getDateDisplayString(parseStrDate(str), locale, mode));
    }

    public String getDomainInOriginalGMTDateDisplayString(OffsetDateTime date, Locale locale) {

        if (date == null)
            return null;

        if (locale.getLanguage().equals("es"))
            return formatter_spa[Month_Day_Year_gmt].format(date);
        else
            return formatter_eng[Month_Day_Year_gmt].format(date);
    }

    /**
     * ZonedDateTime zdt = date.atZoneSameInstant(zid); if
     * (locale.getLanguage().equals("es")) return DateTimeFormatter.ofPattern ("d
     * MMM yyyy x", LOCALE_ES).format(zdt); else return DateTimeFormatter.ofPattern
     * ("MMM d yyyy x", Locale.ENGLISH).format(zdt);
     **/

    // 4
    public String getDateDisplayString(OffsetDateTime date, Locale locale, int mode) {

        if (mode == DATE_MONTH_COLLOQUIAL_FORMAT) {
            if (locale.getLanguage().equals("es"))
                return formatter_spa[Month_Day_Year].format(date);
            return formatter_eng[Month_Day_Year].format(date);
        } else if (mode == DATE_FORMAT_GMT) {

            if (locale.getLanguage().equals("es")) {
                ZoneId zoneId = ZoneId.of(getDefaultTimeZone());
                ZonedDateTime zdt = date.atZoneSameInstant(zoneId);
                String sss = DateTimeFormatter.ofPattern("d MMM yyyy z", Locale.ENGLISH).format(zdt);
                return sss;
            }
            try {
                ZoneId zoneId = ZoneId.of(getDefaultTimeZone());
                ZonedDateTime zdt = date.atZoneSameInstant(zoneId);
                String sss = DateTimeFormatter.ofPattern("MMM d yyyy z", Locale.ENGLISH).format(zdt);
                return sss;

            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (locale.getLanguage().equals("es"))
            return formatter_spa[Month_Day_Year].format(date);
        return formatter_eng[Month_Day_Year].format(date);
    }

    // 4
    public String getStr_ISO_OFFSET_DATE_TIME(OffsetDateTime date) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date);
    }

    /**
     * works for ZonedDateTime, does not for OffsetDateTime Output: 2018-02-05
     * 15:08:00.555-08
     **/
    public String getStr_HIBERNATE_OFFSET_DATE_TIME(ZonedDateTime to) {
        return hibernate.format(to);
    }

    /**
     * works for ZonedDateTime, does not for OffsetDateTime Output format:
     * 2020-03-23 19:36:37.484585-03
     **/
    public String getStr_POSTGRES_OFFSET_DATE_TIME(ZonedDateTime to) {
        return postgres_df.format(to);
    }

    // 1
    public String getDateDisplayString(Date date) {
        return getDateDisplayString(date, Locale.getDefault(), DATE_MONTH_COLLOQUIAL_FORMAT);
    }

    // 2
    public String getDateDisplayString(Date date, Locale locale) {
        return getDateDisplayString(date, locale, DATE_MONTH_COLLOQUIAL_FORMAT);
    }

    // 3
    public String getDateDisplayString(Date date, Locale locale, int mode) {

        if (mode == DATE_MONTH_COLLOQUIAL_FORMAT) {
            if (locale.getLanguage().equals("es"))
                return legacy_date_colloquial_displayformat_es.format(date);
            return legacy_date_colloquial_displayformat_en.format(date);
        }

        if (locale.getLanguage().equals("es"))
            return legacy_date_number_displayformat_es.format(date);

        return legacy_date_number_displayformat_en.format(date);
    }

    public String getStr_ISO_OFFSET_DATE_TIME(Date date, ZoneOffset zo) {
        OffsetDateTime offsetDateTime = date.toInstant().atOffset(zo);
        return legacy_offsetdatedatetime_iso_offset_date_time.format(offsetDateTime);
    }

    // 4
    public String getStr_ISO_OFFSET_DATE_TIME(Date date) {
        OffsetDateTime offsetDateTime = date.toInstant().atOffset(ZoneOffset.UTC);
        return legacy_offsetdatedatetime_iso_offset_date_time.format(offsetDateTime);
    }

    /**
     * DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd
     * HH:mm:ss"); LocalDateTime datetime =
     * LocalDateTime.parse(remotevalue.getDisplayName(), dateformat); OffsetDateTime
     * localvalue = OffsetDateTime.of(datetime, OffsetDateTime.now().getOffset());
     *
     * @param str_date
     * @return
     */

    /**
     * The idea of this function is to guess the format. If the format is known a
     * regular DateTimeFormatter should be used.
     *
     * @param str_date
     * @return
     */

    // static final private int TIMESTAMP_LEN =
    // "2019-08-21T00:00:00-03:00".length();
    public OffsetDateTime parseStrDate(String str_date) {

        if (str_date == null)
            return null;
        // "yyyy-mm-dd"
        if (str_date.length() == DATE_LEN) {
            try {
                /**
                 * returns the 0 hours of the date in Server's GMT
                 */
                LocalDate local = LocalDate.parse(str_date, DateTimeFormatter.ISO_DATE);
                LocalDateTime ldt = local.atStartOfDay();
                return OffsetDateTime.of(ldt, OffsetDateTime.now().getOffset());
            } catch (Exception e) {
                logger.error(e, " | " + str_date);
            }
        }

        try { // 2019-08-21T00:00:00-03:00
            return OffsetDateTime.parse(str_date, DateTimeFormatter.ISO_OFFSET_DATE_TIME); // 2011-12-03T10:15:30+01:00
        } catch (Exception e) {
            logger.error(e, " | " + str_date);
        }

        if (str_date.length() == LOCAL_TSTAMP_LENGTH) { // yyyy-MM-dd HH:mm:ss
            try {
                logger.debug(str_date);
                LocalDateTime datetime = LocalDateTime.parse(str_date, local_tstamp);
                return OffsetDateTime.of(datetime, OffsetDateTime.now().getOffset());
            } catch (Exception e) {
                logger.error(e, " | Tried timestamp without GMT. " + str_date);
            }
        }

        try {
            String xstr = str_date.substring(0, DATE_LEN); // DATE_LEN
            LocalDate local = LocalDate.parse(xstr, DateTimeFormatter.ISO_DATE);
            LocalDateTime ldt = local.atStartOfDay();
            return OffsetDateTime.of(ldt, OffsetDateTime.now().getOffset());
        } catch (Exception e) {
            logger.error(e, " | can not parse the date. returns null " + str_date);
        }
        return null;
    }

    /**
     *
     */

    public Map<String, String> getOrderedZoneIds() {

        if (this.ordered_zones != null)
            return this.ordered_zones;

        this.ordered_zones = new LinkedHashMap<>();
        this.map_zones = new HashMap<String, String>();

        List<String> zoneList = new ArrayList<>(ZoneId.getAvailableZoneIds());

        // Get all ZoneIds
        //
        Map<String, String> allZoneIds = getAllZoneIds(zoneList);

        allZoneIds.entrySet().stream().sorted(new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                try {
                    if (o1.getKey().toLowerCase().startsWith("us") && !o2.getKey().toLowerCase().startsWith("us"))
                        return -1;
                    if (o2.getKey().toLowerCase().startsWith("us") && !o1.getKey().toLowerCase().startsWith("us"))
                        return 1;

                    int comp = o1.getValue().compareToIgnoreCase(o2.getValue());

                    if (comp < 0)
                        return -1;
                    if (comp > 0)
                        return 1;
                    return (o1.getKey().compareToIgnoreCase(o2.getKey()));
                } catch (Exception e) {
                    logger.error(e);
                    return 0;
                }

            }
        }).forEachOrdered(e -> this.ordered_zones.put(e.getKey(), e.getValue()));

        this.ordered_zones.forEach((k, v) -> {
            this.map_zones.put(k, v);
        });
        return this.ordered_zones;
    }

    /**
     * @param zd
     * @return
     */
    public String format(ZonedDateTime zd) {

        Locale lo = getDefaultLocale();

        if (lo.getLanguage().equals("es")) {
            int formatter = Day_Month_Year_hh_mm_ss_zzz;
            return formatter_spa[formatter].format(zd);
        } else {

            int formatter = Month_Day_Year_hh_mm_ss_zzz;
            return formatter_eng[formatter].format(zd);
        }
    }

    /**
     *
     */

    public String formatFileSize(long size) {

        Locale locale = getDefaultLocale();

        return formatFileSize(size, locale);
    }

    public String formatFileSize(long size, Locale locale) {
        return formatFileSize(size, locale, null);
    }

    /**
     * @param size
     */
    public String formatFileSize(long size, Locale locale, String css) {

        try {
            NumberFormat nf_dec = NumberFormat.getInstance(locale);
            nf_dec.setMinimumFractionDigits(2);
            nf_dec.setMaximumFractionDigits(2);
            nf_dec.setRoundingMode(RoundingMode.HALF_UP);

            NumberFormat nf_int = NumberFormat.getInstance(locale);
            nf_int.setMinimumFractionDigits(0);
            nf_int.setMaximumFractionDigits(0);
            nf_int.setRoundingMode(RoundingMode.HALF_UP);

            String css_open = css != null ? "<span class= \"" + css + "\" >" : "";
            String css_close = css != null ? "</span>" : "";

            if (size == 0)
                return nf_int.format(size).trim() + css_open + " KB" + css_close;
            if (size < KB)
                return nf_int.format(size).trim() + css_open + " bytes" + css_close;
            if (size < MB)
                return nf_dec.format((double) size / (double) KB).trim() + css_open + " KB" + css_close;

            else if (size < GB) {
                if (size < 99 * MB)
                    return nf_dec.format((double) size / (double) MB).trim() + css_open + " MB" + css_close;
                else
                    return nf_int.format((double) size / (double) MB).trim() + css_open + " MB" + css_close;
            } else
                return nf_dec.format((double) size / (double) GB).trim() + css_open + " GB" + css_close;
        } catch (Exception e) {
            logger.error(e);
            return e.getClass().getName();
        }
    }

    /**
     * key : Colloquial US/Central value : -05:00
     */
    private static Map<String, String> getAllZoneIds(List<String> zoneList) {
        Map<String, String> result = new HashMap<>();
        LocalDateTime dt = LocalDateTime.now();
        for (String zoneId : zoneList) {
            ZoneId zone = ZoneId.of(zoneId);
            ZonedDateTime zdt = dt.atZone(zone);
            ZoneOffset zos = zdt.getOffset();
            // replace Z to +00:00
            String offset = zos.getId().replaceAll("Z", "+00:00");
            result.put(zone.toString(), offset);
        }
        return result;
    }

    protected User getSessionUser() {
        try {
            return ServiceLocator.getService(SecurityService.class).getSessionUser();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Session User or SystemDefault
     *
     * @return
     */
    private Locale getDefaultLocale() {
        Locale locale = null;
        User user = getSessionUser();
        if (user != null)
            locale = user.getLocale();
        if (locale == null)
            locale = Locale.getDefault();
        return locale;
    }

    private String getDefaultTimeZone() {
        User user = getSessionUser();
        if (user != null)
            return user.getTimeZone();
        return ZoneId.systemDefault().toString();
    }

    private ZoneId getDefaultZoneId() {
        return getDefaultZoneId(null);
    }

    private ZoneId getDefaultZoneId(String zoneId) {

        if (zoneId != null)
            return ZoneId.of(zoneId);

        ZoneId zid = null;

        User user = getSessionUser();
        if (user != null)
            zid = user.getZoneId();
        if (zid == null)
            zid = ZoneId.systemDefault();
        return zid;
    }

}
