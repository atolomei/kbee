package com.novamens.content.web.report.console;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.service.ObjectModelService;
import kbee.web.query.UserAlertsReportQuery;
import kbee.web.report.ReportConsole;
import kbee.web.report.Row;
import kbee.web.report.ReportConsole.InnerIntegerColumn;

public class UserAlertsReportConsole extends ReportConsole {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserAlertsReportConsole.class.getName());
	
	private List<GridColumn<SearchResult,String>> columns = null;

	
	public UserAlertsReportConsole() {
		this("UserAlertsReport", null, "UserAlertsReport");
	}
	
	public UserAlertsReportConsole(String id) {
			this(id, null, id);
	}
	
	public UserAlertsReportConsole(String id, String key) {
		this(id, null, key);
}
	
	public UserAlertsReportConsole(String id, Query query, String key) {
		super(id, query, key);
		//setConsole(id);
		setReportGroup(AUDIT);
	}

	
	
	
	private String getAlertTypeStr(String type) {
		switch (type) { 
			case "10" : return NotificationType.SYSTEM.getLabel(getSessionUser().getLocale()); 
			case "20" : return NotificationType.WORK_NOTE.getLabel(getSessionUser().getLocale()); 
			case "30" : return NotificationType.WORK_NOTE_BILLBOARD.getLabel(getSessionUser().getLocale()); 
			case "40" : return NotificationType.CONTENT.getLabel(getSessionUser().getLocale()); 
			case "50" : return NotificationType.CONDITION.getLabel(getSessionUser().getLocale()); 
			default: return "label not found for" +  type;
		}
	}
	
	
	
	
	public List<GridColumn<SearchResult, String>> getColumns() {

		if (this.columns!=null)
			return this.columns;

		// SerializableSupplier<String> formatSupplier = () -> DateTimeService.MONTH_DAY_YEAR_LABEL;
		 
			this.columns = new ArrayList<GridColumn<SearchResult,String>>();
			
			// this.columns.add(new InnerStringColumn("content_oid", getLabel("column.content_oid"), null));
			// this.columns.add(new InnerStringColumn("content_version", getLabel("column.content_version"), null));
			
			this.columns.add(new InnerStringColumn("title", getLabel("column.title"), "title"));
			
			this.columns.add(new InnerStringColumn("dateread", getLabel("column.dateread"), "dateread"));
			
			
			this.columns.add(new InnerStringColumn("isalert", getLabel("column.isalert"), null));
			this.columns.add(new InnerStringColumn("isbillboard", getLabel("column.isbillboard"), null));

			this.columns.add(new InnerStringColumn("type", getLabel("column.type"), null) {
				private static final long serialVersionUID = 1L;
				protected IModel<String> getLabelModel(Row row) {
					return  new Model<String>( getAlertTypeStr(row.get(getId())));
				}
			});
			

			this.columns.add(new InnerStringColumn("datesend", getLabel("column.datesend"), "datesent"));
			
			this.columns.add(new InnerStringColumn("startpub", getLabel("column.startpub"), null));
			this.columns.add(new InnerStringColumn("endpub", getLabel("column.endpub"), null));
			
			
			
			this.columns.add(new InnerStringColumn("sendername", getLabel("column.sendername"), null));
			// this.columns.add(new InnerStringColumn("contentid", getLabel("column.contentid"), null));
			
			LinkPredicateKbeeGridColumn<Row> linkColumn =	new LinkPredicateKbeeGridColumn<Row>("contentlink",  getLabel("column.contenttitle"), 
						null, 
						obj -> getContentTitle(obj.get("contentid")), 
						obj -> new RowModel(obj));
				
				linkColumn.setContextKey(this.getName() + linkColumn.getContextKey());
				this.columns.add(linkColumn);

				columns.forEach(item -> item.getContextKeyDebug());
				
			return this.columns;
	}
	
	

	
	private String getContentTitle(String cid) {
		if (cid==null || cid.length()==0)
			return "";
		try {
		Long id = Long.valueOf(cid);
		Content content = getContentDao().findContentById(id);
		if (content==null)
			return "";
		
		return content.getTitle();
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getSimpleName();
		}

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
	protected void onBeforeRefresh(AjaxRequestTarget target) {
		super.onBeforeRefresh(target);
		
		/**	
		GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
		logger.debug(panel.getId());
		boolean is_list = (getQuery().getParameters().get("type")!=null && getQuery().getParameters().get("type").equals("list"));
		for (GridColumn<SearchResult, String> col: panel.getColumns()) {
			switch (col.getId()) {
				case "dow": col.setEnabled(is_list); break;
				case "date": col.setEnabled(is_list); break;
				case "time": col.setEnabled(is_list); break;
				case "version": col.setEnabled(is_list); break;
				case "total": col.setEnabled(!is_list); break;
			}
		}
		panel.resetColumns();
		**/
	}
	
	
/**
 *  Contenido
 *  Desde 
 *  Hasta
 */
	@Override
	protected ConsoleSidePanel getRightPanel() {
		
		/**
		 * User 
		 */
		return new UserDateRangeSideReportSidePanel("side", getKey()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
				for (String key : parameters.keySet()) {
					getQuery().getParameters().put(key, parameters.get(key));
				}
				refresh(target);
			}

			protected IModel<String> getUserSelectorLabel() {
				return UserAlertsReportConsole.this.getLabel("receiver");
			}
			
			protected IModel<String> getContentSelectorHelp() {
				return UserAlertsReportConsole.this.getLabel("optional");
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
				getBrowser().togglePanel(UserDateRangeSideReportSidePanel.class);
				UserAlertsReportConsole.this.refresh(target);
				fire(new SidePanelEvent(target));
			}


		};
	}
	
	@Override
	public Query newQuery() {
		return new UserAlertsReportQuery(this.getName()); 
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
			return "user-alerts" + getStringFromTo();
		} catch (Exception e) {
			logger.error(e);
			return super.getDownloadFileName();
		}
	}
	
	

	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<Row>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Row> event) {
				Page page= getPage(event.getModel());
				if (page!=null)
					setResponsePage(page);
			}
		});
	}
	
	protected Content getContent(String id) {
		return getContentDao().findContentById(Long.valueOf(id));
	}
	
	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	protected Page getPage(IModel<Row> rowModel) {
		
		if (rowModel.getObject().get("contentid")==null)
			return null;
		
		Long id = Long.valueOf(rowModel.getObject().get("contentid").toString() );
		
		Content content = getContentDao().findContentById(id);
		
		Page page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page", ServiceLocator.getService(ObjectModelService.class).getObjectModel(content));
		//, false,	true
		return page;
	}

	
	 

	



}
