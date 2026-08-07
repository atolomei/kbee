package com.novamens.content.web.console.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.event.LogEvent;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.AuditDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class AuditActivityPage extends ConsolePage<LogEvent> {
	
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_auditor				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.AUDITOR.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	public AuditActivityPage() {
		super(null);
		setPagePreferences();
	}
	
	public AuditActivityPage(Query query) {
		super(query);
		setPagePreferences();
	}
	
	@Override
	public Console<LogEvent> newConsole(Query query) {

		return new AuditActivityConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return AuditActivityPage.this.getConsolePage(query, index);
			}
		};
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new AuditDropDownBC());
		bc.addElement(new BCElement("audit.activity"));
		panel.setBreadcrumbPanel(bc);
		panel.setTitle( new StringResourceModel("audit.activity", this, null));


		setSearchPlaceHolder(new StringResourceModel("audit.activity", this, null).getObject());
		
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
	
	/**
	 * 
	 * No standard top Toolbar
	 * 
	 */
	@Override
	protected boolean isOpenHeader() {
		return true;
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new AuditActivityPage(query);
	}
	
	@Override
	public Page getConsolePage() {
		 return new AuditActivityPage();
	}
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_support || is_auditor; 
	}
	
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		user.getService(PreferencesService.class).setValue( "settings", "logs",  getClass().getSimpleName());
	}
}
