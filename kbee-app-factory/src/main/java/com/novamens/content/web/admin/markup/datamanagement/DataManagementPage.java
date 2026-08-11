package com.novamens.content.web.admin.markup.datamanagement;


import com.novamens.content.entity.Person;
import com.novamens.content.web.admin.markup.SystemInfoPanel;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

public class DataManagementPage extends ApplicationPage<Person> {
 		
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean service_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	
	private static final long serialVersionUID = 1L;

	public DataManagementPage() {
			
		Person person = getPerson();
		
		if (person!=null) {
			setTopNavigation(getMainTopbar());    
			setMenu(getMainLaternalMenu());       
			setModel(new ObjectModel<Person>(person));
			addComponents(); 
		}
		else 
			add(new ErrorPanel("info-panel", "person not found", ""));
	}

	@Override
	public String getPageHelpKey() {
		return "DataManagementPage";
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}
	
	private void addComponents() {
		if (hasPermissions())
			add(new SystemInfoPanel("info-panel"));
		else
			add(new ErrorPanel("info-panel", "person not found", ""));
	}
	
	@Override
	protected boolean hasPermissions() {
		return (is_root || role_admin || service_admin) && isDomainKbee();
	}
}
