package com.novamens.content.web.admin.markup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.datetime.DateTimeService;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportQuery;
import kbee.web.report.Row;


public class AuditEmailReportQuery extends ReportQuery {

	private static final long serialVersionUID = 1L;
																						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditEmailReportQuery.class.getName());

	public AuditEmailReportQuery() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		setParameters(parameters);
	}
	

	public String getTitle() {
		return "Audit Email";
	};

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
			
			while (resultSet.next() && i < MAX_REPORT_ROWS && errors < 100) {
				try {
					Row row = new Row();
					row.put("action",   resultSet.getString("action"));
					row.put("total",    String.valueOf(resultSet.getInt("total")));
					rows.add(row);
				} catch (Exception e) {
					errors++;
					Row r=getErrorRow(rows, e.getClass().getSimpleName());
					if (r!=null) rows.add(r);
					logger.error(e);
				}
				i++;
			}
			if (i >= MAX_REPORT_ROWS) {
				logger.error("Attention: reached query limit items ");
				Row r=getErrorRow(rows, "Attention: reached query limit items ");
				if (r!=null) rows.add(r);

			}

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

		ZoneId zid = ZoneId.of(getSessionUser().getTimeZone());

        if (zid == null && getDomain().getTimeZone()!=null)
            zid = ZoneId.of(getDomain().getTimeZone());
        
        if (zid == null)
            zid = ZoneId.systemDefault();


        OffsetDateTime date_from = getFrom().truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime date_to = getTo().truncatedTo(ChronoUnit.DAYS).plusSeconds(86400);

        String tostr 	= ServiceLocator.getService(DateTimeService.class).format(date_to,  zid.getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);
        String fromstr 	= ServiceLocator.getService(DateTimeService.class).format(date_from,  zid.getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);

		
        String range = "event_time>'"+fromstr+"' and event_time<='"+tostr+"' ";
        String domain_range = "";
        if (!getDomain().getName().equals("kbee"))        	
        	domain_range = "and event_domain_id = " + String.valueOf(getDomain().getId());
        
		String tablename = "kb_sendemailevent";
		
		stm.append(  "select  extract(year from event_time) \"xyear\",  "
				   + "        event_generator_action \"action\", "
				   + "        count(*) \"Total\"   "
				   + " from " + tablename 
				   + " where " + range + " " + domain_range + " "
				   + " group by event_generator_action, extract(year from event_time) "
				   + " order by \"xyear\" desc, \"action\"");
		logger.debug(stm.toString());
		return stm.toString();
	}

}
