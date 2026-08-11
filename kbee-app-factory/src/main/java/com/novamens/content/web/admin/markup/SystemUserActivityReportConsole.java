package com.novamens.content.web.admin.markup;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.kbee.wicket.markup.html.console.grid.DateKbeeColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.NumberFormatter;
import kbee.web.report.DateRangeReportSidePanel;
import kbee.web.report.ReportQuery;
import kbee.web.report.Row;
import kbee.web.report.SystemReportConsole;
import org.danekja.java.util.function.serializable.SerializableSupplier;

/**
 * 
 *
 */
public class SystemUserActivityReportConsole extends SystemReportConsole {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemUserActivityReportConsole.class.getName());
	

	public class SystemUserActivityReportConsoleQuery extends ReportQuery {

		private static final long serialVersionUID = 1L;

		public SystemUserActivityReportConsoleQuery() {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParameters(parameters);
		}

		public String getTitle() {
			return "User Activity";
		};

	    public OffsetDateTime getFrom() {
	        return getParameters().get("from") == null ? OffsetDateTime.now() : (OffsetDateTime) getParameters().get("from");
	     }

	     public OffsetDateTime getTo() {
	       return getParameters().get("to") == null ? OffsetDateTime.now() : (OffsetDateTime) getParameters().get("to");
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
				int errors=0;
				while (resultSet.next() && i < MAX_REPORT_ROWS && errors < 100) {
					Row row = new Row();
					try {
						row.put("lastname",  resultSet.getString("lastname"));
						row.put("firstname", (resultSet.getString("firstname")!=null?resultSet.getString("firstname"):""));
						row.put("username", resultSet.getString("username"));
						row.put("login", NumberFormatter.formatNumber(resultSet.getInt("Login")));
						row.put("taskstart", NumberFormatter.formatNumber(resultSet.getInt("TaskStart")));
						row.put("taskend", NumberFormatter.formatNumber(resultSet.getInt("TaskEnd")));
						row.put("edit", NumberFormatter.formatNumber(resultSet.getInt("Edit")));
						row.put("publish", NumberFormatter.formatNumber(resultSet.getInt("Publish")));
						row.put("download", NumberFormatter.formatNumber(resultSet.getInt("Download")));
						row.putValue("lasttaskend", resultSet.getObject("LastTaskEnd", OffsetDateTime.class));
						rows.add(row);
						
					} catch (Exception e) {
						
						Row r=getErrorRow(rows, e.getClass().getSimpleName());
						if (r!=null) rows.add(r);

						errors++;
						logger.error(e);
					}

					i++;
					
				}

				if (i >= MAX_REPORT_ROWS)
					logger.error("Attention: reached query limit  items ");

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

		private String getReportStatement() {

			StringBuffer stm = new StringBuffer();

			String tablename = "logevent";
			
			ZoneId zid = ZoneId.of(getSessionUser().getTimeZone());

	        if (zid == null && getDomain().getTimeZone()!=null)
	            zid = ZoneId.of(getDomain().getTimeZone());
	        
	        if (zid == null)
	            zid = ZoneId.systemDefault();
	        

	        OffsetDateTime date_from = getFrom().truncatedTo(ChronoUnit.DAYS);
	        		
	        // OffsetDateTime date_from = getFrom().toInstant().atZone(zid).toOffsetDateTime();
			// date_from = date_from.truncatedTo(ChronoUnit.DAYS);
	        // OffsetDateTime date_to = getTo().toInstant().atZone(zid).toOffsetDateTime();
			// date_to = date_to.truncatedTo(ChronoUnit.DAYS).plusSeconds(86400);
	        
	        OffsetDateTime date_to = getTo().truncatedTo(ChronoUnit.DAYS).plusSeconds(86400);

	        String tostr 	= ServiceLocator.getService(DateTimeService.class).format(date_to,  zid.getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);
	        String fromstr 	= ServiceLocator.getService(DateTimeService.class).format(date_from,  zid.getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT);
			
	        String range = "event_time>'"+fromstr+"' and event_time<='"+tostr+"' ";
	        
	        String domain_range = "";
	        
	        String did = (String) getParameters().get("domain");
	        
	        if (!getDomain().getName().equals("kbee")) {        	
	        	domain_range = "and event_domain_id = " + String.valueOf(getDomain().getId());
	        	// domain_range = "and event_domain_id = " + String.valueOf(getDomain().getId());
	        	
	        }
	        else {
	        	if (did!=null)
	        		domain_range = "and event_domain_id = " + did;
	        }
	        
	        String orderby  = "";
	        
	        if (getParameters().get("sort")!=null) {
	        	String st= (String) getParameters().get("sort");
	        	if (st.equals("name"))      		orderby 	= " order by (lower(lastname), lower(firstname)) ";
	        	else if (st.equals("username"))		orderby 	= " order by username ";
	        	else if (st.equals("signin"))		orderby 	= " order by \"Login\" ";
	        	else if (st.equals("task-start"))	orderby 	= " order by \"TaskStart\" ";
	        	else if (st.equals("task-end"))		orderby 	= " order by \"TaskEnd\" ";
	        	else if (st.equals("publish"))		orderby 	= " order by \"Publish\" ";
	        	else if (st.equals("download"))		orderby 	= " order by \"Download\" ";
				else if (st.equals("lasttaskend"))		orderby 	= " order by \"LastTaskEnd\" ";


	        	else			        			orderby 	= " order by (lower(lastname), lower(firstname)) ";
	        	
	        }
	        else
	        	orderby 	= " order by lower(lastname) ";

	        if (getParameters().get("ascending")!=null) {
	        	if ("false".equals(getParameters().get("ascending"))) 
					orderby = orderby + " desc";
	        }

			stm.append(

			"select lastname, firstname,  username, \"Login\",	 \"Publish\",	 \"TaskEnd\",	 \"TaskStart\",	 \"Download\",	 \"Edit\",	 \"LastTaskEnd\"" +
			"from Users,"+ 
			"("+
			"select COALESCE( user1,user2, user3, user4, user5, user6) uid, \"Login\", \"Publish\", \"TaskEnd\", \"TaskStart\", \"Download\", \"Edit\",	 \"LastTaskEnd\" " +
			" from" +   
			"(" +
			"	("+ 
			"		(select event_user \"user1\", count(*) \"Login\"          from "+tablename+" where " + range + " and event_type =  'LoginEvent' "   + domain_range + " group by event_user)    Q1 FULL JOIN "+ 
			"		(select event_user \"user2\", count(*)  \"Publish\"       from "+tablename+" where " + range + " and event_type =  'CheckinEvent' " + domain_range + " group by event_user)  Q2  " +
			"		 ON Q1.user1 = Q2.user2 " +
			"	)  A " +
			"	FULL JOIN " + 
			"	( " +
			"		 (select event_user \"user3\", count(*)  \"TaskEnd\",  max(event_time)  \"LastTaskEnd\"      from "+tablename+" where " + range  + " and event_type =  'TaskEndEvent' "  + domain_range + " group by event_user)     Q3 Full Join " +
			"		 (select event_user \"user4\", count(*)  \"TaskStart\"     from "+tablename+" where " + range  + " and event_type =  'TaskStartEvent' "+ domain_range + " group by event_user)   Q4  " +
			" 	   on Q3.user3 = Q4.user4 " +
			"	) B  " +
			"	ON coalesce(A.user1, A.user2) = coalesce(B.user3, B.user4) " +
			") XA " +
			"Full Join " +
			"( " +
			"	(select event_user \"user5\", count(*)  \"Download\"      from "+tablename+" where " + range + "  and event_type like  'Download%' "+ domain_range + " group by event_user)     Q5 Full Join " + 
			"	(select event_user \"user6\", count(*)  \"Edit\"  from "+tablename+" where "+ range + "  and  (event_type like  'Update%') " + domain_range +  " group by event_user) Q6  " +
			"	ON Q5.user5=Q6.user6 " +
			") XB  " +
			"ON coalesce(XB.user5, XB.user6) =coalesce(XA.user1, XA.user2) " +
			") QER  " +
			"where QER.uid=Users.id " +
			orderby 
			);

			logger.debug(stm.toString());
			
			return stm.toString();
		}
	};
	
	
	/**      
	 * 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<GridColumn<SearchResult,String>> columns = null;
	
	
	public SystemUserActivityReportConsole() {
		this("SystemUserActivityReport");
	}
	
	
	public SystemUserActivityReportConsole(String id) {
		super(id, null, id);
		setReportGroup(SYSTEMAUDIT);
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new InnerStringColumn("lastname", 			 getLabel("lastname"), "name"));
		this.columns.add(new InnerStringColumn("firstname", 		 getLabel("firstname")));
		this.columns.add(new InnerStringColumn("username", 			 getLabel("username"), "username"));
		this.columns.add(new InnerNumberColumn("login", 			 getLabel("signin"), "signin", "number-md", true));
		this.columns.add(new InnerNumberColumn("publish", 			 getLabel("publish"), "publish" , "number-mdx", true));
		this.columns.add(new InnerNumberColumn("taskstart", 		 getLabel("taskstart"), "task-start", "number-mdx", true));
		this.columns.add(new InnerNumberColumn("taskend", 			 getLabel("taskend"), "task-end", "number-mdx", true));
		this.columns.add(new InnerNumberColumn("edit",		 		 getLabel("edit"),  "edit", "number-mdx", true));
		this.columns.add(new InnerNumberColumn("download", 			 getLabel("downloadgrid"), "download", "number-md", true));
		{
			SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
			DateKbeeColumn<Row> submittedDate = new DateKbeeColumn<Row>("lasttaskend", getLabel("lasttaskend"), (row) -> (OffsetDateTime) row.getValue("lasttaskend"), formatSupplier);
			columns.add(submittedDate);
		}
		return this.columns;
	}
	
	
 	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
 	
	@Override
	public Query newQuery() {
		return new SystemUserActivityReportConsoleQuery();
	}
	
	@Override
	public boolean isReadable() {
		return 
		 (	ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) ||
		    ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId())||
			ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()) );
	}
	
	@Override
	public String getDownloadFileName() {
		DateFormat dateparameterformat = new SimpleDateFormat("YYYY-MM-dd");
		return  "audit-user-activity-" + dateparameterformat.format(new Date());		
		
	}

	@Override
	public IModel<String> getReportDescription() {
		return new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return "For the Date range [From - to] the system uses User´s Time Zone. See "; 
			}
		};
	}
	
	@Override
	protected ConsoleSidePanel getRightPanel() {
		
		DateRangeReportSidePanel pa = (DateRangeReportSidePanel) super.getRightPanel();
		
		pa.setDomainSelector(true);
		
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime fst=now.minusDays(0);
		pa.setOffsetDateTimeFrom(fst);
		pa.setOffsetDateTimeTo(now);
		
		//Date fromdate = new Date(fst.toInstant().toEpochMilli());
		//pa.setFrom(fromdate);
		//Date todate = new Date();
		//pa.setTo(todate);
		
		getQuery().getParameters().put("from", pa.getOffsetDateTimeFrom());
		getQuery().getParameters().put("to", pa.getOffsetDateTimeTo());
		
		return pa;
	}
	
}
