package kbee.web.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.sql.OraclePlatform;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.web.report.ReportQuery;
import kbee.web.report.Row;

/**
 * alerts received by a user
 */
public class UserAlertsReportQuery extends ReportQuery {

	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserAlertsReportQuery.class.getName());

    public UserAlertsReportQuery(String reportName) {

    	Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("domain", getDomain().getId());
        parameters.put("reportName", reportName);
        setParameters(parameters);
    }

    public String getTitle() {
        return this.getClass().getSimpleName();
    }

    public String getReportName() {
        return getParameters().get("reportName") == null ? "": (String) getParameters().get("reportName");
    }

    
	protected List<Row> getRows() {

		ArrayList<Row> rows = new ArrayList<Row>();

		if (getParameters().get("from") == null || getParameters().get("to") == null || getParameters().get("user") == null) {
			logger.debug("some of from, to, user missing");
			return rows;
		}

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {

			connection = this.getDataSource().getConnection();
			String getstatement = this.getReportStatement(getSqlPlatform(connection));

			statement = connection.prepareStatement(getstatement);
			resultSet = statement.executeQuery();
			int i = 0;

			// User offset as of now
			DateTimeService d_service = ServiceLocator.getService(DateTimeService.class);
			String zid = d_service.getMapZoneIds().get(getSessionUser().getTimeZone());
			if (zid == null)
				zid = ZoneId.systemDefault().getId();
			ZoneId zone = ZoneId.of(zid);

			//DateTimeFormatter date_f = DateTimeFormatter.ofPattern("d MMM YYYY", getSessionUser().getLocale());
			//DateTimeFormatter time_f = DateTimeFormatter.ofPattern("HH:mm:ss", getSessionUser().getLocale());
			//DateTimeFormatter dweek_f = DateTimeFormatter.ofPattern("EEEE", getSessionUser().getLocale());

			DateTimeFormatter date_f = DateTimeFormatter.ofPattern("d MMM YYYY HH:mm:ss", getSessionUser().getLocale());
			
			// User offset as of now
			ZoneOffset currentOffsetForMyZone = zone.getRules().getOffset(Instant.now());

			int errors=0;
			while (resultSet.next() && i < MAX_REPORT_ROWS && errors < 100) {
            	try {
	            	Row row = new Row();

	            	
	            		row.put("sendername", 
	            			    (resultSet.getString("sender_firstname")!=null? (resultSet.getString("sender_firstname")+" "):"") 
	            			    + resultSet.getString("sender_lastname")
	            			    );
	            		
	            		
						// row.put("sender_firstname", );
						// row.put("sender_username", resultSet.getString("sender_username"));

						// content if applicable
						//
						row.put("contentid", resultSet.getString("content_id"));
						// row.put("content_version", resultSet.getInt("version") > 0 ? String.valueOf(resultSet.getInt("version")) : "");
						
						// alert
						//
						row.put("title", resultSet.getString("title"));
						row.put("isalert", resultSet.getBoolean("isalert") ? "yes" : "no");
						row.put("isbillboard", resultSet.getBoolean("isbillboard") ? "yes" : "no");
						row.put("type", resultSet.getInt("type") > 0 ? String.valueOf(resultSet.getInt("type")) : "");
						
						/**
						Timestamp datesend = resultSet.getTimestamp("sent");
						Instant datesend_is = datesend.toInstant();
						OffsetDateTime datesend_datetime = OffsetDateTime.ofInstant(datesend_is, currentOffsetForMyZone);
						row.put("datesend", date_f.format(datesend_datetime));
						
						Timestamp dateread = resultSet.getTimestamp("dateread");
						Instant dateread_is =  dateread.toInstant();
						OffsetDateTime dateread_datetime = OffsetDateTime.ofInstant(dateread_is, currentOffsetForMyZone);
						row.put("dateread", date_f.format(dateread_datetime));
						
						Timestamp startpub = resultSet.getTimestamp("startpub");
						Instant startpub_is =startpub.toInstant();
						OffsetDateTime startpub_datetime = OffsetDateTime.ofInstant(startpub_is, currentOffsetForMyZone);
						row.put("startpub", date_f.format(startpub_datetime));
							*/
						
						row.put("datesend",	resultSet.getTimestamp("sent")  !=null ? date_f.format(OffsetDateTime.ofInstant(resultSet.getTimestamp("sent").toInstant()  , currentOffsetForMyZone)): "");
						row.put("dateread",	resultSet.getTimestamp("dateread")  !=null ? date_f.format(OffsetDateTime.ofInstant(resultSet.getTimestamp("dateread").toInstant()  , currentOffsetForMyZone)): "");
						row.put("startpub",	resultSet.getTimestamp("startpub")!=null ? date_f.format(OffsetDateTime.ofInstant(resultSet.getTimestamp("startpub").toInstant(), currentOffsetForMyZone)): "");
						row.put("endpub",	resultSet.getTimestamp("endpub")  !=null ? date_f.format(OffsetDateTime.ofInstant(resultSet.getTimestamp("endpub").toInstant()  , currentOffsetForMyZone)): "");
						
										
						logger.debug(row.toString());
						
	            		rows.add(row);
	            	
	            	
            	} catch (Exception e) {
            		logger.error(e);
            		errors++;
					Row r=getErrorRow(rows, e.getClass().getSimpleName());
					if (r!=null) rows.add(r);

            	}
                i++;
			}
            
            if (i >= MAX_REPORT_ROWS) {
            	logger.error("Attention: reached query limit items ");
            	Row r=getErrorRow(rows, "Reached query limit items ");
            	if (r!=null) rows.add(r);
            	
            }
		}

		catch (SQLException e) {
			logger.error(e);
        	Row r=getErrorRow(rows, e.getClass().getSimpleName() +  " | " + e.getMessage());
        	if (r!=null) rows.add(r);
        	
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

                
    
	 
 
	/**
	 * 
	 * @param platform
	 * @return
	 */

	private String getReportStatement(SqlPlatform platform) {

		StringBuffer stm = new StringBuffer();

		OffsetDateTime to;
		OffsetDateTime from;

		String receiver_id = null, sender_id = null, type = null;
		
		if (getParameters().get("from") != null)
			from = ((OffsetDateTime) getParameters().get("from")).truncatedTo(ChronoUnit.DAYS);
		 else
			from = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);

		if (getParameters().get("to") != null) 
			to = ((OffsetDateTime) getParameters().get("to")).plusDays(1).truncatedTo(ChronoUnit.DAYS);
		else
			to = OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);;

			
		if (getParameters().get("sender_id") != null) 
				sender_id = (String) getParameters().get("sender_id").toString();
				
		if (getParameters().get("type") != null) 
				type = (String) getParameters().get("type").toString();

		
		String content_oid = null;
		
		if (getParameters().get("contentOid") != null) 
			content_oid = (String) getParameters().get("contentOid").toString();
		

		if (getParameters().get("user") != null) 
			receiver_id = (String) getParameters().get("user").toString();
		else
			receiver_id=getSessionUser().getId().toString();
		
		DateTimeFormatter df;
		
		if (platform instanceof OraclePlatform)
			df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		else
			df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sx", Locale.ENGLISH);

		String fromstr = df.format(from);
		String tostr = df.format(to);

		if (platform instanceof OraclePlatform) {
			
			/**
			stm.append("select visit_time \"time\", user_name \"user\" , content_id, version from po_sitelogin where ");
			stm.append(" visit_time >= to_timestamp('" + fromstr + "', 'YYYY-MM-DD HH24:MI:SS') and ");
			stm.append(" visit_time <  to_timestamp('" + tostr + "', 'YYYY-MM-DD HH24:MI:SS') ");
			stm.append(" and (content_oid=" + content_id);
			if (getParameters().get("sort") != null) {
				if (getParameters().get("sort").equals("time"))
					stm.append(" order by visit_time ");
				else if (getParameters().get("sort").equals("user"))
					stm.append(" order by user_id, visit_time ");
			} else {
				stm.append(" order by visit_time ");
			}
			if (getParameters().get("ascending") != null) {
				if ("false".equals(getParameters().get("ascending"))) {
					stm.append(" desc");
				}
			}
			 	*/
			throw new KbeeRuntimeException("Not implemented");
		
		}

		else {
				
				//
				// this is the Sender, Receiver does not need a join because it is only 1 (receiver_id)´
				//
			//
			// Type
			// is read or not ?
			// sender
				
			
			
			
			
			stm.append("    select K.notification_type \"type\",  "
					+ "            lastname \"sender_lastname\", "
					+ "            firstname \"sender_firstname\", "
					+ "            username \"sender_username\", "
						+ "        K.title,  "
						+ "        K.content_id, "
						+ "        K.datesend \"sent\", "
						+ "        K.dateread, K.isalert, "
						+ "        K.isbillboard, "
						+ "        K.startpub, "
						+ "        K.endpub "
						
						+ "        from users U, "
		                + "                     ( select title, "
		                + "                              receiver_id, "
		                + "                              content_id, "
		                + "                              datesend, "
		                + "                              dateread, "
		                + "                              notification_type, "
		                + "                              isalert, "
		                + "                              isbillboard, "
		                + "                              startpub, "
		                + "                              endpub, "
		                + "                              sender_id "
		                + "                        from kb_notification "
		                
		                + "                        where datesend>='"+fromstr+"' and datesend<'"+tostr+"' "   
		                +                                (sender_id!=null?       (" and sender_id="+sender_id) : "" )
		                +                                (type!=null?            (" and type="+type) : "" )
		                +                                (content_oid!=null?     (" and content_id in (select id from content where oid="+content_oid+")") : "" )
		                + "                        ) K "
		                
				        + " where U.id=K.sender_id and K.receiver_id=" + receiver_id  
					);
			

			if (getParameters().get("sort") != null) {
					
					if (getParameters().get("sort").equals("datesent"))
						stm.append(" order by sent ");	
					else if (getParameters().get("sort").equals("dateread"))
						stm.append(" order by dateread ");
					else if (getParameters().get("sort").equals("title"))
						stm.append(" order by title ");
					else
						stm.append(" order by sent ");
					
				} else {
					stm.append(" order by sent ");
				}
				
				if (getParameters().get("ascending") != null) {
					if ("false".equals(getParameters().get("ascending"))) {
						stm.append(" desc");
					}
				}
			
		}

		logger.debug(stm.toString());

		return stm.toString();
	}
    

}
