package kbee.web.report;

import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;


import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class SystemReportConsole extends ReportConsole {

	private static final long serialVersionUID = 1L;
	
	public SystemReportConsole(String id, Query query) {
					this(id, query, null);
	}
	
	public SystemReportConsole(String id, Query query, String key) {
		super(id, query, key);
		setReportGroup(SYSTEMAUDIT);
	}
 
	/**
	 * 
	 */
	@Override
	protected ConsoleSidePanel getRightPanel() {
		
		ReportBaseParameterPanel panel = new DateRangeReportSidePanel("side", getKey()) {
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
				getBrowser().togglePanel( DateRangeReportSidePanel.class );
				SystemReportConsole.this.refresh(target);
				fire(new SidePanelEvent(target));
			}
		};
		
		for (String key : panel.getParameters().keySet())
			getQuery().getParameters().put(key, panel.getParameters().get(key));
		
		return panel;
	}

	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		return null;
	}
	
	
	@Override
	public boolean isReadable() {
		return isDomainKbee() && 
		 (	ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) ||
		    ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId())||
			ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()) );
		
	}
}
