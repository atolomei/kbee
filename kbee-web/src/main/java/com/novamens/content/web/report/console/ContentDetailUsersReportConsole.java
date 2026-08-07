package com.novamens.content.web.report.console;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.dashboard.LabelPanel;
import kbee.web.query.ContentDetailUsersReportQuery;
import kbee.web.report.ReportConsole;
import kbee.web.report.Row;

			
public class ContentDetailUsersReportConsole extends ReportConsole {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentDetailUsersReportConsole.class.getName());
	
	private List<GridColumn<SearchResult,String>> columns = null;

	
	public ContentDetailUsersReportConsole() {
		this("content-detail-users-report", null, "ContentDetailUsersReport");
	}
	
	public ContentDetailUsersReportConsole(String id) {
			this(id, null, id);
	}
	
	public ContentDetailUsersReportConsole(String id, String key) {
		this(id, null, key);
}
	
	public ContentDetailUsersReportConsole(String id, Query query, String key) {
		super(id, query, key);
		//setConsole(id);
		setReportGroup(AUDIT);
	}

	
	public List<GridColumn<SearchResult, String>> getColumns() {

		if (this.columns!=null)
			return this.columns;


		// SerializableSupplier<String> formatSupplier = () -> DateTimeService.MONTH_DAY_YEAR_LABEL;
		
			this.columns = new ArrayList<GridColumn<SearchResult,String>>();
			
			this.columns.add(new InnerStringColumn("dow", getLabel("column.dow"), null));
			this.columns.add(new InnerStringColumn("date", getLabel("column.date"),"time"));
			this.columns.add(new InnerStringColumn("time", getLabel("column.time"),"time"));
										
			
			
			
			this.columns.add(new InnerStringColumn("lastname", getLabel("column.lastname"), "user"));
			// this.columns.add(new InnerStringColumn("firstname", getLabel("column.firstname"), null));
			this.columns.add(new InnerStringColumn("username", getLabel("column.username"), "username"));
			
			this.columns.add(new InnerStringColumn("site_title", getLabel("column.portal"), "portal"));
			
			this.columns.add(new InnerIntegerColumn("version", getLabel("column.version"),null));
			this.columns.add(new InnerIntegerColumn("total", getLabel("column.total"), "total"));
		
			
			boolean is_list = (getQuery().getParameters().get("type")!=null && getQuery().getParameters().get("type").equals("list"));
			
			for (GridColumn<SearchResult,String> col: columns) {
				switch (col.getId()) {
					case "dow": col.setEnabled(is_list); break;
					case "date": col.setEnabled(is_list); break;
					case "time": col.setEnabled(is_list); break;
					case "version": col.setEnabled(is_list); break;
					case "site_title": col.setEnabled(is_list); break;
					
					case "total": col.setEnabled(!is_list); break;
					
				}
			}
			
		return this.columns;
	}
	
	public IModel<String> getDisplayName() {															
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString( getKey(), getSessionUser().getLocale()));
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}

	
	
	@Override
	protected void onBeforeRefresh(AjaxRequestTarget target) {
		
		GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
		logger.debug(panel.getId());
		boolean is_list = (getQuery().getParameters().get("type")!=null && getQuery().getParameters().get("type").equals("list"));
		for (GridColumn<SearchResult, String> col: panel.getColumns()) {
			switch (col.getId()) {
				case "dow": col.setEnabled(is_list); break;
				case "date": col.setEnabled(is_list); break;
				case "time": col.setEnabled(is_list); break;
				case "version": col.setEnabled(is_list); break;
				case "site_title": col.setEnabled(is_list); break;
				
				case "total": col.setEnabled(!is_list); break;
				
			}
		}
		panel.resetColumns();
	}
	
	
/**
 *  Contenido
 *  Desde 
 *  Hasta
 */
	@Override
	protected ConsoleSidePanel getRightPanel() {
		
		return new ContentDateRangeSideReportSidePanel("side", getKey(), getQuery().getParameters()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
				for (String key : parameters.keySet()) {
					getQuery().getParameters().put(key, parameters.get(key));
				}
				refresh(target);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Map<String, Object> parameters) {
				for (String key : parameters.keySet()) {
					getQuery().getParameters().put(key, parameters.get(key));
				}
				refresh(target);
			}
			@Override
			public void onClose(AjaxRequestTarget target) {
				getBrowser().togglePanel(ContentDateRangeSideReportSidePanel.class);
				 ContentDetailUsersReportConsole.this.refresh(target);
				fire(new SidePanelEvent(target));
			}


		};
	}
	
	@Override
	public Query newQuery() {
		
		//UserAlertsReportQuery temp = new UserAlertsReportQuery(this.getName()); 
		//temp.getParameters().put("from", OffsetDateTime.now().minusDays(1));
		//temp.getParameters().put("to", OffsetDateTime.now());
		//temp.getParameters().put("receiver", getSessionUser().getId().toString());
		//List<Row> rows= temp.getRows();
		
		ContentDetailUsersReportQuery q= new ContentDetailUsersReportQuery(this.getName());
		q.getParameters().put("type", "list");
		return q;
	}
	

	@Override
	public boolean isReadable() {
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		boolean role_admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean role_portal = service.isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());
		boolean role_support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
		return role_admin || role_support || role_portal;
	}
	 
	
	@Override
	public String getDownloadFileName() {
		try {
			return "content-users-" + getStringFromTo();
		} catch (Exception e) {
			logger.error(e);
			return super.getDownloadFileName();
		}
	}
	
	protected Content getContent(String id) {
		return getContentDao().findContentById(Long.valueOf(id));
	}

	
	protected Attribute getAttribute(String name) {
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.getName() != null && attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}

	@Override
	protected Panel getItemListPanel(IModel<Row> model, int index) {
		return new LabelPanel( "item", new Label("label", model.getObject().toString()));
	}

}
