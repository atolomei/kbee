package com.novamens.content.web.console.markup;

import org.apache.wicket.Page;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.document.TreeFile;
import com.novamens.indexer.query.Query;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.AuditDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
			
public class AuditTreeFileResourcesPage extends ConsolePage<TreeFile> {

	private static final long serialVersionUID = 1L;
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public AuditTreeFileResourcesPage() {
		super(null);
	}
	
	public AuditTreeFileResourcesPage(Query query) {
		super(query);
	}
	
	@Override
	public Console<TreeFile> newConsole(Query query) {
		return new AuditTreeFileResourcesConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return AuditTreeFileResourcesPage.this.getConsolePage(query, index);
			}
		};
	}


	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new AuditDropDownBC());
		bc.addElement(new BCElement("audit.tree"));
		panel.setBreadcrumbPanel(bc);
		panel.setTitle( new StringResourceModel("audit.tree", this, null));

		setSearchPlaceHolder(new StringResourceModel("audit.tree", this, null).getObject());
		
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(true); // button advanced search
		panel.setSearchPanel(getSearchPanel());
		setPageContentHeader(panel);
		

	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new AuditTreeFileResourcesPage(query);
	}

	@Override
	public Page getConsolePage() {
		return new AuditTreeFileResourcesPage();
	}

	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.LOGS;
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root; 
	}
}
