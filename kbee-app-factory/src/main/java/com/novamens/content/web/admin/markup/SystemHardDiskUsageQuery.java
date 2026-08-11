package com.novamens.content.web.admin.markup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportQuery;
import kbee.web.report.Row;

public class SystemHardDiskUsageQuery extends ReportQuery {
			
	private static final long serialVersionUID = 1L;
																		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemHardDiskUsageQuery.class.getName());

	public SystemHardDiskUsageQuery() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		setParameters(parameters);
	}

	public String getTitle() {
		return "Hard Disk Usage";
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

			boolean isKbee = getDomain().getName().equals("kbee" );
			
			int errors=0;
			while (resultSet.next() && i < MAX_REPORT_ROWS && errors<100) {
				
				try {
					Row row = new Row();
					if (resultSet.getTimestamp("date")!=null) {
						Instant instant = Instant.ofEpochMilli(resultSet.getTimestamp("date").getTime());
						OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
						String s=ServiceLocator.getService(DateTimeService.class).format(dt, ZoneId.of(getSessionUser().getTimeZone()).getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);
						row.put("date",  s);
					}
					else 
						row.put("date",  "");
					row.put("hd_total", 					String.valueOf( resultSet.getDouble("hd_total") / 1000000000.0));
					row.put("hd_total_db", 					String.valueOf( resultSet.getDouble("db_total") / 1000000000.0));
					row.put("hd_total_gateway",    			String.valueOf( resultSet.getDouble("hd_gateway") / 1000000000.0));
					row.put("contents",    					String.valueOf( resultSet.getInt("contents")));
					row.put("resources",    				String.valueOf( resultSet.getInt("resources")));
					row.put("users",    					String.valueOf( resultSet.getInt("users")));
					row.put("File System", 				   		String.valueOf( resultSet.getLong("File System")/ 1000000000.0 ));
					row.put("Minio",    					String.valueOf( resultSet.getLong("Minio")/ 1000000000.0));
					row.put("resources_external",    		String.valueOf( resultSet.getLong("resources_external")));
					if (isKbee) {
						row.put("solr_content",    		String.valueOf( resultSet.getInt("solr_content")));
						row.put("solr_file",    		String.valueOf( resultSet.getInt("solr_file")));
						row.put("solr_audit",    		String.valueOf( resultSet.getInt("solr_audit")));
					}
					i++;
					rows.add(row);
				} catch (Exception e) {
					
					Row r=getErrorRow(rows, e.getClass().getSimpleName());
					if (r!=null) rows.add(r);

					errors++;
					logger.error(e);
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
	 * 
	 * 
	 * select  EXTRACT(EPOCH FROM ts) "date",    hard_disk_usage/1000000000.0  hd_total from kb_usage_stat where domain_id= 1 and   (ts>'2018 10 01 02:00:00 ART' and ts<='2020 09 10 21:00:00 ART')  order by ts
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
 	 */
	private String getReportStatement() {

		StringBuffer stm = new StringBuffer();
		String tablename = "kb_usage_stat";
		
		ZoneId zid = ZoneId.of(getSessionUser().getTimeZone());

        if (zid == null && getDomain().getTimeZone()!=null)
            zid = ZoneId.of(getDomain().getTimeZone());
        
        if (zid == null)
            zid = ZoneId.systemDefault();


        OffsetDateTime date_from = getFrom().truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime date_to = getTo().truncatedTo(ChronoUnit.DAYS).plusSeconds(86400);

        String tostr 	= ServiceLocator.getService(DateTimeService.class).format(date_to,  zid.getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);
        String fromstr 	= ServiceLocator.getService(DateTimeService.class).format(date_from,  zid.getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);

        String domainWhere = "";
        
        Domain domain = getDomain();
        
        if (domain.getName().equals("kbee"))
        	domainWhere = "domain_id= " + String.valueOf((Long) domain.getId()) + " and ";
        else
        	domainWhere = "domain_id= " + String.valueOf((Long) domain.getId()) + " and ";
        
        String range = " (ts>'"+fromstr+"' and ts<='"+tostr+"') ";
        
        String sortdir = " desc ";
		if (getParameters().get("ascending")!=null) {
			if ("false".equals(getParameters().get("ascending"))) {
				sortdir =" asc ";
			}
			else {
				sortdir =" desc ";
			}
		}

        
        
        // String 
        
		stm.append(  "select ts \"date\", "
				   + "       hard_disk_usage hd_total, "
				   + "       database_usage db_total, "
				   + "       hard_disk_usage_gateway hd_gateway, "
				   + "       contents contents, "
				   + "       resources resources, "
				   + "       users users, "
				   + "       kbfs1_hard_disk_usage kbfs1, "
				   + "       kbfs2_hard_disk_usage kbfs2, "
				   + "       odilon_hard_disk_usage odilon, "
				   + "       resources_external resources_external "
				   + (domain.getName().equals("kbee") ? ", solr_content_items solr_content,  solr_file_items solr_file,	solr_audit_items solr_audit ":"")
				   + "	from " + tablename 
				   + " where " + domainWhere + " " + range 
				   + " order by ts " + sortdir );

		logger.debug(stm.toString());
		return stm.toString();
	}

}
