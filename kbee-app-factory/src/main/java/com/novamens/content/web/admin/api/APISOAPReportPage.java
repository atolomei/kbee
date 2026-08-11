package com.novamens.content.web.admin.api;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.content.web.sql.markup.SQLFiltersPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.APISOAPReportBC;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;

public class APISOAPReportPage extends ApplicationPage<Person>implements FactoryPage {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(APISOAPReportPage.class.getName());
	
	private final boolean admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private final boolean role_service_admin   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	private final boolean role_api_developer   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	private final boolean role_factory_manager = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	
	public APISOAPReportPage() {
		setPageTitle(new ResourceModel("mainmenu.domains.api.soap.report"));
		Person person = getPerson();
		if (person!=null) {
			
			setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
			setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
			
			
			setModel(new ObjectModel<Person>(person));
			addComponents(); 
		}
		else {
			add(new ErrorPanel("info-panel", "Not authorized", ""));
		}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		if (isDomainKbee())
			return ApplicationMenuSection.API;
		return ApplicationMenuSection.LOGS;
		
	}
	
	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder();
		if (isDomainKbee()) {
			str.append("<p><b>Service Admin</b> <b>API Developer</b>can access this Page. ");
			str.append("You need <b>Service Admin</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		} else {
			str.append("You need <b>Domain Admin</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		}
		return str.toString();
	}
	
	/**
	 * Service Admin
	 */
	@Override
	protected boolean hasPermissions() {
		
		if (getSecurityService().isRoot())
			return true;
		
		if (isDomainKbee()) 
			return (role_service_admin || role_api_developer || role_factory_manager);
		
		return admin;
	}
	
	@Override
	protected boolean isFooterRequired() {
		return false;
	}
	
	private com.novamens.service.SecurityService getSecurityService() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class);
	}
	
	private void addBreadcrumb() {
		 MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		 bc.addElement(new APIBC());
		 bc.addElement(new APISOAPReportBC());
		 add(bc);
	}
	
	private void addComponents() {
		addBreadcrumb();
		if (hasPermissions()) {
			APISOAPReportSelectorPanel selector = new APISOAPReportSelectorPanel("filters");
			selector.getQuery();
			SQLFiltersPanel panel = new SQLFiltersPanel("panel", selector.getQuery());
			panel.addFiltersSelectorPanel(selector);
			add(panel);
		}
		else
			add(new ErrorPanel("panel", "Not authorized", ""));
	}
}
