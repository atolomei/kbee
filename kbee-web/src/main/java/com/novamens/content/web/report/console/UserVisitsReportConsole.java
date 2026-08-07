package com.novamens.content.web.report.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.service.ObjectModelService;
import kbee.web.query.UserVisitsReportQuery;
import kbee.web.report.ReportConsole;
import kbee.web.report.Row;

public class UserVisitsReportConsole extends ReportConsole {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserVisitsReportConsole.class.getName());
	
	private List<GridColumn<SearchResult,String>> columns = null;

	
	public UserVisitsReportConsole() {
		this("UserVisitsReport", null, "UserVisitsReport");
	}
	
	public UserVisitsReportConsole(String id) {
			this(id, null, id);
	}
	
	public UserVisitsReportConsole(String id, String key) {
		this(id, null, key);
}
	
	public UserVisitsReportConsole(String id, Query query, String key) {
		super(id, query, key);
		// setConsole(id);
		setReportGroup(AUDIT);
	}

	public List<GridColumn<SearchResult, String>> getColumns() {

		if (this.columns!=null)
			return this.columns;

		// SerializableSupplier<String> formatSupplier = () -> DateTimeService.MONTH_DAY_YEAR_LABEL;
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		{
			LinkPredicateKbeeGridColumn<Row> linkColumn =	new LinkPredicateKbeeGridColumn<Row>("contenttitle", 
					getLabel("column.content_title"), 
					"content", 
					obj -> obj.get("contenttitle"), 
					obj -> new RowModel(obj));
			
			linkColumn.setContextKey(this.getName() + linkColumn.getContextKey());
			this.columns.add(linkColumn);
		}

		this.columns.add(new InnerStringColumn("dow", getLabel("column.dow"), "time"));
		this.columns.add(new InnerStringColumn("date", getLabel("column.date"),"time"));
		this.columns.add(new InnerStringColumn("time", getLabel("column.time"),"time"));
		this.columns.add(new InnerStringColumn("sitetitle", getLabel("column.site_title"),null));
		this.columns.add(new InnerStringColumn("xid", getLabel("column.xid"), "content_oid"));
		this.columns.add(new InnerIntegerColumn("version", getLabel("column.version"),null));
		return this.columns;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}

	
	public IModel<String> getDisplayName() {															
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString( getKey(), getSessionUser().getLocale()));
	}
	
/**
 *  Contenido
 *  Desde 
 *  Hasta
 */
	@Override
	protected ConsoleSidePanel getRightPanel() {
		
		return new UserDateRangeSideReportSidePanel("side", getKey()) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
				
				//if (((AdvancedSearchField) get("form:content")).getValue()==null) {
				//}
				
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
				getBrowser().togglePanel(UserDateRangeSideReportSidePanel.class);
				UserVisitsReportConsole.this.refresh(target);
				fire(new SidePanelEvent(target));
			}
		};
	}
	
	@Override
	public Query newQuery() {
		return new UserVisitsReportQuery(this.getName());
	}
	

	@Override
	public String getDownloadFileName() {
		try {
			return "user-visits-" + getStringFromTo();
		} catch (Exception e) {
			logger.error(e);
			return super.getDownloadFileName();
		}
	}
	
	
	protected Content getContent(String id) {
		return getContentDao().findContentById(Long.valueOf(id));
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
	
	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	protected Page getPage(IModel<Row> rowModel) {
	
		if (rowModel.getObject().get("contentid")==null)
			return null;
		
		Long id = Long.valueOf(rowModel.getObject().get("contentid").toString() );
		
		Content content = getContentDao().findContentById(id);
		
		Page page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page", 
				ServiceLocator.getService(ObjectModelService.class).getObjectModel(content), 
				false, 
				true);
		
		
		
		
		return page;
	}
	
	 

}
