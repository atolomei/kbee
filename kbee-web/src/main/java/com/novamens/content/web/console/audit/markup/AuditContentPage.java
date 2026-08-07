package com.novamens.content.web.console.audit.markup;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.Role;
import com.novamens.event.LogEvent;
import com.novamens.indexer.query.Query;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.AuditDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public  class AuditContentPage extends ConsolePage<LogEvent> {
	private static final long serialVersionUID = 1L;
	
	final boolean is_root				= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_auditor			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.AUDITOR.getId());
	final boolean is_support			= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	public AuditContentPage() {
		super(null);
	}
	
	public AuditContentPage(Query query) {
		super(query);
	}
	
	@Override
	public Console<LogEvent> newConsole(Query query) {
		return new AuditContentConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return AuditContentPage.this.getConsolePage(query, index);
			}
		};
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new AuditDropDownBC());
		bc.addElement(new BCElement("audit.content"));
		panel.setBreadcrumbPanel(bc);
		panel.setTitle( new StringResourceModel("audit.content", this, null));

		setSearchPlaceHolder(new StringResourceModel("audit.content", this, null).getObject());
		
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(true); // button advanced search
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<LogEvent> toolbar = new PageTaskToolbar<LogEvent>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.LOGS;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new AuditContentPage(query);
	}
	
	@Override
	public Page getConsolePage() {
		return new AuditContentPage();
	}
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_support || is_auditor; 
	}

}