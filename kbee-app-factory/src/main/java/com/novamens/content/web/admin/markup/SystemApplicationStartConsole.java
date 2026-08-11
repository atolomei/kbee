package com.novamens.content.web.admin.markup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

import kbee.util.NumberFormatter;
import kbee.web.report.ReportQuery;
import kbee.web.report.Row;
import kbee.web.report.SystemReportConsole;

public class SystemApplicationStartConsole extends SystemReportConsole {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemApplicationStartConsole.class.getName());
	
	/***
	 * 
	 */
	public class SystemApplicationStartQuery extends ReportQuery {

		private static final long serialVersionUID = 1L;

		public SystemApplicationStartQuery() {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParameters(parameters);
		}

		public String getTitle() {
			return "Application Start Query";
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
				int errors=0;
				while (resultSet.next() && i < MAX_REPORT_ROWS && errors<100) {
					try {
						Row row = new Row();
						if (resultSet.getTimestamp("ts")!=null) {
							Instant instant = Instant.ofEpochMilli(resultSet.getTimestamp("ts").getTime());
							OffsetDateTime dt = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
							String s=ServiceLocator.getService(DateTimeService.class).format(dt, ZoneId.of(getSessionUser().getTimeZone()).getId(), getSessionUser().getLocale(), DateTimeService.Year_Month_Day);
							row.put("timestamp", s);
						}
						else
							row.put("timestamp", "");
						String ms=resultSet.getString("event_parameters");
						row.put("miliseconds", ms!=null?  NumberFormatter.formatNumber(Long.valueOf(ms), getSessionUser().getLocale()):"");
						rows.add(row);
					} catch (Exception e) {
						errors++;
						Row r=getErrorRow(rows, e.getClass().getSimpleName());
						if (r!=null) rows.add(r);
						logger.error(e);
					}
					i++;
				}

				if (i >= 20000) {
					logger.error("Attention: reached query limit items ");
					Row r=getErrorRow(rows, "Attention: reached query limit items ");
					if (r!=null)
						rows.add(r);
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

			String tablename = "logevent";

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
			
			stm.append(   "select  event_time \"ts\",  "
						+ " event_parameters "
						+ " from " + tablename 
					    + " where   event_type='ApplicationStartEvent' and (" + range + ") "
					    + " order by event_time desc");

			logger.debug(stm.toString());
			return stm.toString();
		}
	};
	
	
	/**      -----------------------------------------------------------------
	 * 
	 * 
	 * 
	 * 
	 */
		
	private static final long serialVersionUID = 1L;
	
	private List<GridColumn<SearchResult,String>> columns = null;
	 
	public SystemApplicationStartConsole() {
		this("SystemApplicationStartReport");
	}
	
	public SystemApplicationStartConsole(String id) {
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
		this.columns.add(new InnerStringColumn("timestamp", 		new Model<String>("Date")));
		this.columns.add(new InnerNumberColumn("miliseconds", 		new Model<String>("Startup (ms)"), null));
		return this.columns;
	}
	
	public IModel<String> getDisplayName() {															
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString( getKey(), getSessionUser().getLocale()));
	}
	
 	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}

	@Override
	public Query newQuery() {
		return new SystemApplicationStartQuery();
	}
	

	@Override
	public Domain getDomain() {
		return getContentDao().findDomainByName("kbee");
	}
	
	@Override
	public String getDownloadFileName() {
		DateFormat dateparameterformat = new SimpleDateFormat("YYYY-MM-dd");
		return  "audit-application-start-" + dateparameterformat.format(new Date());		
		
	}

}
