package kbee.web.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ReportSubscriptorsConsole extends SystemReportConsole {
																										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportSubscriptorsConsole.class.getName());
	
				
	public class ReportSubscriptorsQuery extends ReportQuery {
		
		private static final long serialVersionUID = 1L;

		public ReportSubscriptorsQuery() {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParameters(parameters);
		}

		public String getTitle() {
			return "Report Subscription Query";
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
				
				String yes= getLabel("yeshtml").getObject();
				String no= getLabel("nohtml").getObject();
				
				int errors=0;
				while (resultSet.next() && i++ < MAX_REPORT_ROWS && errors < 100) {
					Row row = new Row();
					try {
						row.put("lastname",  resultSet.getString("lastname"));
						row.put("firstname", (resultSet.getString("firstname")!=null?resultSet.getString("firstname"):""));
						row.put("sub", resultSet.getString("sub"));
						row.put("enabled", resultSet.getBoolean("enabled") ? yes : no );
						if (resultSet.getTimestamp("sent")!=null) {
							Instant instant_sent = Instant.ofEpochMilli(resultSet.getTimestamp("sent").getTime());
							OffsetDateTime dt_sent = OffsetDateTime.ofInstant(instant_sent, ZoneId.systemDefault());
							row.put("sent", ServiceLocator.getService(DateTimeService.class).format(dt_sent, ZoneId.of(getSessionUser().getTimeZone()).getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT));
						}
						else
							row.put("sent", "");
						if (resultSet.getTimestamp("modified")!=null) {
							Instant instant = Instant.ofEpochMilli(resultSet.getTimestamp("modified").getTime());
							OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
							row.put("modified", ServiceLocator.getService(DateTimeService.class).format(dt, ZoneId.of(getSessionUser().getTimeZone()).getId(), getSessionUser().getLocale(), DateTimeService.Full_GMT));
						}
						else {
							row.put("modified", "");
						}
						rows.add(row);
						
					} catch (Exception e) {
						errors++;
						Row r=getErrorRow(rows, e.getClass().getSimpleName());
						if (r!=null) rows.add(r);
						logger.error(e);
					}
				}
				if (i >= MAX_REPORT_ROWS)
					logger.error("Attention: reached query limit " + String.valueOf(MAX_REPORT_ROWS)+ " items ");

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

		/**
		 * 
		 */
		private String getReportStatement() {

			StringBuffer stm = new StringBuffer();
	        String tableSchedule 	= "kb_report_subscription";
	        String tableUsers 		= "users";

	        
	        
	        String des; 
	        String orderby;
	        
	        if (getParameters().get("sort")!=null) {
	        	String st= (String) getParameters().get("sort");
	        	if (st.equals("sub"))      		orderby 	= " order by (RS.report_export_sched_id, U.lastname, U.firstname)  ";
	        	else if (st.equals("sent"))		orderby 	= " order by RS.last_export_sent ";
	        	else if (st.equals("name"))		orderby 	= " order by (lower(U.lastname), lower(U.firstname),RS.report_export_sched_id)";
	        	else if (st.equals("modified")) orderby 	= " order by RS.lastmodifieddate ";
	        	else			        		orderby 	= " order by (U.lastname, U.firstname, RS.report_export_sched_id) ";
	        }
	        else
	        	orderby 	= " order by (U.lastname, U.firstname, RS.report_export_sched_id) ";

	        if (getParameters().get("ascending")!=null) {
	        	if ("false".equals(getParameters().get("ascending"))) 
					orderby = orderby + " desc";
	        }
	        
	        stm.append("select  RS.report_export_sched_id \"sub\", "
	        		+ "         U.lastname \"lastname\", "
	        		+ "         U.firstname \"firstname\", "
	        		+ "         RS.last_export_sent \"sent\", "
	        		+ "         RS.enabled \"enabled\", "
	        		+ "         RS.lastmodifieddate  \"modified\" from " + tableSchedule + " RS, " + tableUsers + " U where RS.usr=U.id and RS.domain_id = " + getDomain().getId().toString() + orderby);
	        logger.debug(stm.toString());
			return stm.toString();
		}
	};

	
	/**
	 *       
	 */
	private static final long serialVersionUID = 1L;
	
	private List<GridColumn<SearchResult,String>> columns = null;
	 
	public ReportSubscriptorsConsole() {
		this("ReportSubscriptorsConsole", null, "ReportSubscriptorsConsole");
	}
	
	
	public ReportSubscriptorsConsole(String id) {
		this(id, null, id);
	}
	
	public ReportSubscriptorsConsole(String id, Query query, String key) {
		super(id, query, key);
		//setConsole(id);
		setReportGroup(SYSTEMAUDIT);	
	}
	
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	/**
	 * 
	 */
	public List<GridColumn<SearchResult, String>> getColumns() {
		if (this.columns!=null)
			return this.columns;
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		this.columns.add(new InnerStringColumn("lastname", 			new Model<String>("Lastname"), "name"));
		this.columns.add(new InnerStringColumn("firstname", 		new Model<String>("Firstname")));
		this.columns.add(new InnerStringColumn("sub", 				new Model<String>("Subscription"), "sub"));
		this.columns.add(new InnerStringColumn("enabled", 			new Model<String>("Enabled")));
		this.columns.add(new InnerStringColumn("sent", 				new Model<String>("Last Sent"), "sent"));
		this.columns.add(new InnerStringColumn("modified", 			new Model<String>("Modified"), "modified"));
		return this.columns;
	}
	
	@Override
	public IModel<String> getDisplayName() {															
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString(getKey(), getSessionUser().getLocale()));
	}
	
 	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
 	
	@Override
	public Query newQuery() {
		return new ReportSubscriptorsQuery();
	}
	
	@Override
	public boolean isReadable() {
		return  ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	
	@Override
	public String getDownloadFileName() {
		DateFormat dateparameterformat = new SimpleDateFormat("YYYY-MM-dd");
		return  "report-subscriptions-" + dateparameterformat.format(new Date());		
		
	}

	
	@Override
	public IModel<String> getReportDescription() {
		return new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return "Report Subscriptions."; 
			}
		};
	}
	
	@Override
	protected ConsoleSidePanel getRightPanel() {
		return null;
	}
	/**
	@Override
	protected ConsoleSidePanel getRightPanel() {
		
		DateRangeReportSidePanel pa = (DateRangeReportSidePanel) super.getRightPanel();
		
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime fst=now.minusDays(0);
		Date fromdate = new Date(fst.toInstant().toEpochMilli());

		pa.setFrom(fromdate);
		Date todate = new Date();
		pa.setTo(todate);
		
		getQuery().getParameters().put("from", pa.getFrom());
		getQuery().getParameters().put("to", pa.getTo());
		return pa;
	}*/


}
