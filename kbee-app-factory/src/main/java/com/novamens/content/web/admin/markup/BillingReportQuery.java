package com.novamens.content.web.admin.markup;


import com.novamens.datetime.DateTimeService;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportQuery;
import kbee.web.report.Row;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BillingReportQuery extends ReportQuery {

    private static final long serialVersionUID = 1L;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BillingReportQuery.class.getName());

    public BillingReportQuery() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        setParameters(parameters);
    }

    public String getTitle() {
        return "Billing Report";
    }


    private DomainType getType( int intType) {
    
    	switch (intType)  {
    			
    		case 1:  return DomainType.PREMIUM;
    		case 2:  return DomainType.FILE_SYSTEM_READER;
    		case 3:  return DomainType.EXPRESS;
    		case 4:  return DomainType.SYSTEM;
    		
    		case 5:  return DomainType.COMPLIANCE;
    		case 6:  return DomainType.TEMPLATE;
    	};
    	
    	return null;
    	
    }
    protected List<Row> getRows() {

        ArrayList<Row> rows;

        rows = new ArrayList<Row>();

        Connection connection = null;
        PreparedStatement statement = null;
        java.sql.ResultSet resultSet = null;

        try {

            connection = this.getDataSource().getConnection();
            String getstatement = this.getReportStatement();
            statement = connection.prepareStatement(getstatement);
            resultSet = statement.executeQuery();
            int i = 0;
            int errors = 0;
            while (resultSet.next() && i < MAX_REPORT_ROWS && errors<100) {

            	try {
                Row row = new Row();
                if (resultSet.getTimestamp("date") != null) {
                    Instant instant = Instant.ofEpochMilli(resultSet.getTimestamp("date").getTime());
                    OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
                    String s = ServiceLocator.getService(DateTimeService.class).format(dt, ZoneId.of(getSessionUser().getTimeZone()).getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);
                    row.put("date", s);
                } else
                    row.put("date", "");
                
                row.put("domain", resultSet.getString("domain"));
                
                row.put("organization",  resultSet.getString("organization"));
                				
                DomainType tr = getType(resultSet.getInt("type"));
                
                row.put("type", (tr!=null ? tr.getLabel() : "") );
                
                row.put("billable_sites", String.valueOf(resultSet.getInt("billable_sites")));
                row.put("billable_users", String.valueOf(resultSet.getInt("billable_users")));
                row.put("units", String.valueOf(resultSet.getInt("units")));

                row.put("users", String.valueOf(resultSet.getInt("users")));
                row.put("contents", String.valueOf(resultSet.getInt("contents")));
                row.put("hd_total", String.valueOf( resultSet.getDouble("hd_total") / 1000000000.0));
                
                // ---
                // row.put("db_total", String.valueOf( resultSet.getDouble("db_total") / 1000000000.0));
                // ---

                rows.add(row);
            	} catch (Exception e ) {
            		errors++;
	            	logger.error(e);
					Row r=getErrorRow(rows, e.getClass().getSimpleName());
					if (r!=null) rows.add(r);
            	}
                
            }

            if (i >= MAX_REPORT_ROWS)
                logger.error("Attention: reached query limit 20,000 items ");

        } catch (SQLException e) {
            logger.error(e);
            return rows;
        } finally {
            try {
                if (statement != null)
                    statement.close();
                
                if (resultSet != null)
                    resultSet.close();
                
                if (connection != null)
                    connection.close();
                
            } catch (SQLException e) {
                logger.error(e);
                return rows;
            }
        }

        return rows;
    }

    public OffsetDateTime getFrom() {
        return getParameters().get("from") == null ? OffsetDateTime.now() : (OffsetDateTime) getParameters().get("from");
    }

    public OffsetDateTime getTo() {
        return getParameters().get("to") == null ? OffsetDateTime.now() : (OffsetDateTime) getParameters().get("to");
    }

    /**
     *
     */
    private String getReportStatement() {

        StringBuffer stm = new StringBuffer();
        String tablename = "kb_usage_stat";

        ZoneId zid = ZoneId.of(getSessionUser().getTimeZone());

        if (zid == null && getDomain().getTimeZone() != null)
            zid = ZoneId.of(getDomain().getTimeZone());

        if (zid == null)
            zid = ZoneId.systemDefault();

        OffsetDateTime date_from = getFrom().truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime date_to = getTo().truncatedTo(ChronoUnit.DAYS).plusSeconds(86400);
        if (getParameters().containsKey("nLastMonthsPeriods")) {
            Long lnLastMonthPeriods = (Long)getParameters().get("nLastMonthsPeriods");

            date_from  = getDateMonthStart(lnLastMonthPeriods);
            getParameters().put("from", date_from.toInstant());

            date_to  = getDateMonthStart(-1l);
            getParameters().put("to", date_to.toInstant());
        }

        String tostr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date_to);
        String fromstr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date_from);

        
        
        String range = " (ts>'" + fromstr + "' and ts<='" + tostr + "') ";

        
        String orderstr = " ts ";
        
        if (getParameters().get("sort") != null) {
        	orderstr = getParameters().get("sort").toString();
        }
        
        
        String sortdir = " desc ";
        if (getParameters().get("ascending") != null) {
            if ("false".equals(getParameters().get("ascending"))) {
                sortdir = " asc ";
            } else {
                sortdir = " desc ";
            }
        }

        stm.append("select d.name \"domain\","
                
        		+ "       ts \"date\", "

				+ "       d.organization organization, "
				+ "       d.type \"type\", "
                
                + "       billable_sites, "
                + "       billable_users, "
                
				+ "       hard_disk_usage_gateway, "
				+ "       kbfs2_hard_disk_usage, "
				+ "       odilon_hard_disk_usage, "
				+ "       s3_hard_disk_usage, "
				
				 
                + "       hard_disk_usage hd_total, "                
                + "       units, "
                + "       users, "
                + "       contents "
                
                + "	from " + tablename + " st inner join domain d on st.domain_id = d.id "
                + " where " + range + "and d.state=1 and not exists(select 1 from " + tablename + " st2 where st2.domain_id=st.domain_id and st2.ts > st.ts and " + range + ")"
                + " order by " + orderstr +" " + sortdir);

        logger.debug(stm.toString());
        return stm.toString();
    }

    private OffsetDateTime getDateMonthStart(Long lnLastMonthPeriods) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.add(Calendar.MONTH, Math.toIntExact(lnLastMonthPeriods * -1));
        aCalendar.set(Calendar.DATE, 1);
        aCalendar.set(Calendar.HOUR_OF_DAY, 0);
        aCalendar.set(Calendar.MINUTE, 0);
        aCalendar.set(Calendar.SECOND, 0);
        aCalendar.set(Calendar.MILLISECOND, 0);

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(aCalendar.getTimeInMillis()), aCalendar.getTimeZone().toZoneId()).toOffsetDateTime();
    }

}
