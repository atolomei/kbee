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

public class UserVisitsReportQuery extends ReportQuery {

	private static final long serialVersionUID = 1L;
																							
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserVisitsReportQuery.class.getName());
				
    public UserVisitsReportQuery(String reportName) {

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
			logger.debug("incomplete parameters");
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
			//
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
			
			int errors=0;
			while (resultSet.next() && i < MAX_REPORT_ROWS && errors < 100) {
				 try {
						Row row = new Row();
		
						Timestamp ts = resultSet.getTimestamp("time");
						Instant is = ts.toInstant();
						OffsetDateTime candidate = OffsetDateTime.ofInstant(is, currentOffsetForMyZone);
		
						row.put("dow", dweek_f.format(candidate));
						row.put("date", date_f.format(candidate));
						row.put("time", time_f.format(candidate));
						
						row.put("page", resultSet.getString("page"));
						row.put("pagetype", resultSet.getString("page_type"));
						row.put("sitetitle", resultSet.getString("site_title"));
						row.put("contenttitle", resultSet.getString("title"));
						row.put("version", resultSet.getString("version"));
						
						row.put("xid", resultSet.getString("content_oid")+ " / "+resultSet.getString("content_id"));
						
						row.put("contentoid", resultSet.getString("content_oid"));
						row.put("contentid", resultSet.getString("content_id"));
						rows.add(row);
						
						
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
			Row r=getErrorRow(rows, e.getClass().getSimpleName());
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
		String user_id;

		if (getParameters().get("from") != null) {
			from = ((OffsetDateTime) getParameters().get("from")).truncatedTo(ChronoUnit.DAYS);
		} else
			from = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);

		if (getParameters().get("to") != null) {
			to = ((OffsetDateTime) getParameters().get("to")).plusDays(1).truncatedTo(ChronoUnit.DAYS);
		} else
			to = OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);

		if (getParameters().get("user") != null) {
			user_id = (String) getParameters().get("user");
		} else
			user_id = "0";

		String content_oid = null;
		
		if (getParameters().get("contentOid") != null) 
			content_oid = (String) getParameters().get("contentOid").toString();

		
		DateTimeFormatter df;

		if (platform instanceof OraclePlatform)
			df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		else
			df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sx", Locale.ENGLISH);

		String fromstr = df.format(from);
		String tostr = df.format(to);

		if (platform instanceof OraclePlatform) {
			
			throw new KbeeRuntimeException("Oracle not supported");
			
			/**
			stm.append("select visit_time \"time\", page_title \"page\", site_title from po_sitelogin where ");
			stm.append(" visit_time >= to_timestamp('" + fromstr + "', 'YYYY-MM-DD HH24:MI:SS') and ");
			stm.append(" visit_time <  to_timestamp('" + tostr + "', 'YYYY-MM-DD HH24:MI:SS') ");

			stm.append(" and user_id=");
			stm.append(user_id);

			if (getParameters().get("sort") != null) {
				if (getParameters().get("sort").equals("time"))
					stm.append(" order by visit_time ");
				else if (getParameters().get("sort").equals("page"))
					stm.append(" order by page, visit_time ");
			} else {
				stm.append(" order by visit_time ");
			}

			if (getParameters().get("ascending") != null) {
				if ("false".equals(getParameters().get("ascending"))) {
					stm.append(" desc");
				}
			}
			**/
		}

		else {
			
			stm.append("select title, ");
			stm.append("time, "
					+ "page_title as page, "
					+ "page_type, "
					+ "site_title, "
					+ "site_id, "
					+ "page_title, "
					+ "C.id \"content_id\", "
					+ "C.oid \"content_oid\", "
					+ "C.version "
					+ " from content C, ( ");
					 
					stm.append("select visit_time \"time\", "
							+ "page_title as page, "
							+ "page_type, "
							+ "site_title, "
							+ "site_id, "
							+ "page_title, "
							+ "content_title, "
							+ "content_id, "
							+ "content_long_id, "
							+ "version, "
							+ "content_oid from po_sitelogin where ");
					stm.append(" visit_time>='"+fromstr+"' and visit_time<'"+tostr+"'");
					stm.append(" and user_id="+user_id );
					stm.append(" and page_type= 'det'");
					stm.append(content_oid !=null ? (" and content_oid =" + content_oid) : "");
					
					
					stm.append(" ) S ");
			
			stm.append(" where C.id=S.content_long_id ");
			
			if (getParameters().get("sort") != null) {
				if (getParameters().get("sort").equals("time"))
					stm.append(" order by time ");
				else if (getParameters().get("sort").equals("content"))
					stm.append(" order by (title, time) ");
				else if (getParameters().get("sort").equals("page"))
					stm.append(" order by (page, time) ");
				else if (getParameters().get("sort").equals("content_oid"))
					stm.append(" order by (content_oid, time) ");
				else 
					stm.append(" order by time ");
				
			} else {
				stm.append(" order by time ");
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
