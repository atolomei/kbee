package com.novamens.content.web.admin.markup.datamanagement;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.entity.Person;
import com.novamens.content.web.admin.files.DMFilesPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.datamanagement.ReindexContentPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
import kbee.web.service.ApplicationSiteMapService;


public class SystemDataManagementGeneralPage extends ApplicationPage<Person> implements FactoryPage {
			
	
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemDataManagementGeneralPage.class.getName());
	
	boolean role_factory = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()); // Stefanie
	boolean role_service = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId()); // AT
	boolean admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private static final long serialVersionUID = 1L;

	String name;
	PageParameters parameters;
	 
	public SystemDataManagementGeneralPage(PageParameters parameters) {

		StringValue id= parameters.get("id");
		
		if (id!=null) 
			name = id.toOptionalString();
		
		this.parameters = parameters;
		
		init(name);
	}
	
	/**
	 * 
	 * @param panel
	 * @param key
	 */
	
	/**
	 * @param key
	 */

	public SystemDataManagementGeneralPage(String key) {
		this.name = key;
		getPageParameters().add("id", key);
		init(key);
	}

	
	public void setDMFilesPanel(DMFilesPanel panel) {
		addOrReplace(panel);
	}
	
	
	/**
	 * SQL
	 * 
	 * Data Management deprecated
	 * File Explorer
	 * Deploy Manager
	 * 
	 *  
	 */
	
	private void init(String key) {
		
		setPageTitle(new Model<String>(key));
		
		Person person = getPerson();
	
		if (person!=null) {
		
			setTopNavigation(getMainTopbar()); 
			setMenu(getMainLaternalMenu());    
			setModel(new ObjectModel<Person>(person));
			
			if (isDomainKbee()) {
				add(ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel("info-panel", key, parameters));
			}
			else
				add(new ErrorPanel("info-panel", "Domain must be Factory", ""));
		}
		else
			add(new ErrorPanel("info-panel", "person not found", ""));
	}

	
	protected String getName() {
		return name;
	}
	
	@Override
	public String getPageHelpKey() {
		return super.getPageHelpKey()+"-"+getName();
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT; 
	}
	
	@Override
	public boolean hasPermissions() {
		return isDomainKbee() && (isRoot() || isAdmin());  
	}
	
	protected boolean isAdmin() {
		return admin;
	}

	protected boolean isServiceManager() {
		return role_service;
	}
	
	protected boolean isFActoryManager() {
		return role_factory;
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
}
