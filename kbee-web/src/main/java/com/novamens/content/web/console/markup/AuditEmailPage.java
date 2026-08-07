package com.novamens.content.web.console.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.logging.SendEmailEvent;
import com.novamens.security.acl.KbeeGlobalRole;
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
public class AuditEmailPage extends ConsolePage<SendEmailEvent> {
	private static final long serialVersionUID = 1L;
	
	final boolean is_root				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_auditor			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.AUDITOR.getId());
	final boolean is_support			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	public AuditEmailPage() {
		super(null);
	}
	
	public AuditEmailPage(Query query) {
		super(query);
	}
	
	@Override
	public Console<SendEmailEvent> newConsole(Query query) {
 		return new AuditEmailConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return AuditEmailPage.this.getConsolePage(query, index);
			}
		};
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.LOGS;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new AuditEmailPage(query);
	}
	
	@Override
	public Page getConsolePage() {
		return new AuditEmailPage();
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_support || is_auditor; 
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<SendEmailEvent> panel=new PageContentHeaderPanel<SendEmailEvent>(null);
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new AuditDropDownBC());
		bc.addElement(new BCElement("audit.email"));
		panel.setBreadcrumbPanel(bc);
		panel.setTitle( new StringResourceModel("audit.email", this, null));
		setSearchPlaceHolder(new StringResourceModel("audit.email", this, null).getObject());
		
		setSuggester(false);			 	// Search supports suggester
		setSearchPanel(true); 				// include Search

		setAdvancedSearch(true); 			// button advanced search
		
		
		
		//panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<SendEmailEvent> toolbar = new PageTaskToolbar<SendEmailEvent>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);

	}
	
	
	
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
					 
					getQuery().getParameters().put("text", event.getText());
					getQuery().getParameters().put("sort", "relevance");
					setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	


}
