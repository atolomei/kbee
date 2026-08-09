
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.dependencies.Dependency;
import com.novamens.kbee.sql.OraclePlatform;
import com.novamens.kbee.sql.PostgresPlatform;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportQuery;
import kbee.web.report.Row;

public class ContentDetailUsersReportQuery extends ReportQuery {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentDetailUsersReportQuery.class.getName());

    public ContentDetailUsersReportQuery(String reportName) {

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

		if (getParameters().get("from") == null || getParameters().get("to") == null
				|| getParameters().get("content") == null)
			return rows;

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

			DateTimeFormatter date_f = DateTimeFormatter.ofPattern("d MMM YYYY", getSessionUser().getLocale());
			DateTimeFormatter time_f = DateTimeFormatter.ofPattern("HH:mm:ss", getSessionUser().getLocale());
			DateTimeFormatter dweek_f = DateTimeFormatter.ofPattern("EEEE", getSessionUser().getLocale());

			// User offset as of now
			ZoneOffset currentOffsetForMyZone = zone.getRules().getOffset(Instant.now());

            int errors = 0;
            while (resultSet.next() && i < MAX_REPORT_ROWS && errors<100) {

            	try {
	            	Row row = new Row();

	            	if (getParameters().get("type")!=null && getParameters().get("type").equals("list")) {
						Timestamp ts = resultSet.getTimestamp("time");
						Instant is = ts.toInstant();
						OffsetDateTime candidate = OffsetDateTime.ofInstant(is, currentOffsetForMyZone);
						
						row.put("dow", dweek_f.format(candidate));
						row.put("date", date_f.format(candidate));
						row.put("time", time_f.format(candidate));
						row.put("content_id", resultSet.getString("cid"));
						
						row.put("version", resultSet.getInt("version") > 0 ? String.valueOf(resultSet.getInt("version")) : "");
						
						row.put("site_id", resultSet.getString("site_id"));
						row.put("site_title", resultSet.getString("site_title"));
						
	            		row.put("lastname", (resultSet.getString("firstname")!=null?(resultSet.getString("firstname")+" ") :"" ) + resultSet.getString("lastname"));
	            		// row.put("firstname", resultSet.getString("firstname"));
	            		row.put("username", resultSet.getString("username"));
	            		row.put("total", "");
	            		
						rows.add(row);
	            	} else {
					
	            		row.put("lastname", resultSet.getString("lastname"));
	            		row.put("firstname", resultSet.getString("firstname"));
	            		row.put("username", resultSet.getString("username"));
	            		row.put("total", resultSet.getString("total"));
	            		
						row.put("dow", "");
						row.put("date", "");
						row.put("time", "");
						row.put("content_id", "");
						row.put("version", "");

						rows.add(row);

	            	}
	            	
            	} catch (Exception e) {
            		errors++;
            		logger.error(e);
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

			Row row = new Row();
			row.put("dow", e.getClass().getName());
			
			if (getParameters().get("type")!=null && getParameters().get("type").equals("list")) {
			row.put("time", "");
			row.put("date", "");
			row.put("content_id", "");
			row.put("site_id", "");
			row.put("site_title", "");
			row.put("version", "");
			row.put("user", "");
			
			rows.add(row);
			}
			else {
				row.put("user", "");
				row.put("total", "");
			}
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
	 * @param platform
	 * @return
	 */

    private String getReportStatement() {
    	return  getReportStatement( new PostgresPlatform() );
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
		String content_id;
		String view_id;

		if (getParameters().get("from") != null)
			from = ((OffsetDateTime) getParameters().get("from")).truncatedTo(ChronoUnit.DAYS);
		 else
			from = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);

		if (getParameters().get("to") != null) 
			to = ((OffsetDateTime) getParameters().get("to")).plusDays(1).truncatedTo(ChronoUnit.DAYS);
		else
			to = OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);;

		if (getParameters().get("contentOid") != null) 
			content_id = (String) getParameters().get("contentOid").toString();
		else {
		
			
			content_id = "-1";
		}

		if (getParameters().get("view") != null)
			view_id = (String) getParameters().get("view").toString();
		else
			view_id = "0";

		 

		/***
		 * user_id, visit_time
		 * 
		 * CREATE INDEX log_user_id_idx ON po_sitelogin(user_id, visit_time) TABLESPACE
		 * SBD02_INDEX; CREATE INDEX log_page_id_idx ON po_sitelogin(page_id,
		 * visit_time) TABLESPACE SBD02_INDEX; CREATE INDEX log_page_title_idx ON
		 * po_sitelogin(page_title, visit_time) TABLESPACE SBD02_INDEX;
		 * 
		 */
		DateTimeFormatter df;
		if (platform instanceof OraclePlatform)
			df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		else
			df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sx", Locale.ENGLISH);

		String fromstr = df.format(from);
		String tostr = df.format(to);

		if (platform instanceof OraclePlatform) {

			stm.append("select visit_time \"time\", user_name \"user\" , content_id, version from po_sitelogin where ");

			stm.append(" visit_time >= to_timestamp('" + fromstr + "', 'YYYY-MM-DD HH24:MI:SS') and ");
			stm.append(" visit_time <  to_timestamp('" + tostr + "', 'YYYY-MM-DD HH24:MI:SS') ");

			//if (fromstr.compareTo("2017-10-01") > 0 || content_id.equals("-1")) {
			//	stm.append(" and page_id=" + view_id);
			//} else {
			//	stm.append(" and (page_id=" + view_id + " or (page_id=" + content_id
			//			+ " and visit_time<to_timestamp('2017-09-07 00:00:00', 'YYYY-MM-DD HH24:MI:SS')  ))");
			//}
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
		}

		else {
			
			if (getParameters().get("type")!=null && getParameters().get("type").equals("list")) {
				
				stm.append("select lastname, firstname, username, site_id, site_title, UX.visit_time \"time\", UX.content_id \"cid\", UX.version \"version\" from users U, ");
				stm.append("( select visit_time, user_id, content_id, site_id, site_title, version from po_sitelogin where visit_time>='"+fromstr+"' and visit_time<'"+tostr+"' and content_oid=" + content_id + " and page_type='det' ) UX ");
				stm.append(" where U.id=UX.user_id ");
				
				
				if (getParameters().get("sort") != null) {
					
					if (getParameters().get("sort").equals("time")) {
						stm.append(" order by time ");	
					}
					else if (getParameters().get("sort").equals("user"))
						stm.append(" order by (lastname, firstname) ");
					else if (getParameters().get("sort").equals("username"))
						stm.append(" order by (username, visit_time)  ");
					
					
					else if (getParameters().get("sort").equals("portal"))
						stm.append(" order by (site_title, visit_time)  ");
					
				} else {
					stm.append(" order by time ");
				}
				
				if (getParameters().get("ascending") != null) {
					if ("false".equals(getParameters().get("ascending"))) {
						stm.append(" desc");
					}
				}
				
			}
			else {
				
				//
				// Page Type = det for content details pages in Sections and Portal
				//
				stm.append("select lastname, firstname, username, total from users U, ");
				stm.append(" ( select user_id, count(*) total from po_sitelogin where visit_time>='"+fromstr+"' and visit_time<'"+tostr+"' and content_oid=" + content_id +" and page_type='det' group by user_id) UX ");
				stm.append(" where U.id=UX.user_id ");
				
				if (getParameters().get("sort") != null) {
					
					if (getParameters().get("sort").equals("user"))
						stm.append(" order by (lastname, firstname) ");
					else if (getParameters().get("sort").equals("total"))
							stm.append(" order by total ");
					else if (getParameters().get("sort").equals("username"))
						stm.append(" order by username ");
				} else {
					stm.append(" order by (lastname, firstname) ");
				}
				
				if (getParameters().get("ascending") != null) {
					if ("false".equals(getParameters().get("ascending"))) {
						stm.append(" desc");
					}
				}
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
		}

		logger.debug(stm.toString());

		return stm.toString();
	}
    
	/**
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
    **/

}
