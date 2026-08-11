package com.novamens.content.web.integration;
        
import java.io.File;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.FactoryPage;

/**
 *
 */
public class FileSystemIntegrationPage extends ConsolePage<File> implements FactoryPage {
	
	private static final long serialVersionUID = 1L;
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	public FileSystemIntegrationPage() {
		super(null);
		setPageTitle(getLabel("pagetitle"));
	}
				
	public FileSystemIntegrationPage(Query query) {
		super(query);
		setPageTitle(getLabel("pagetitle"));
	}
	
	@Override
	public Console<File> newConsole(Query query) {
		return new FileSystemIntegrationConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return FileSystemIntegrationPage.this.getConsolePage(query, index);
			}
		};
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new FileSystemIntegrationPage(query);
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.INTEGRATION;
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_support; 
	}
	

	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
}
