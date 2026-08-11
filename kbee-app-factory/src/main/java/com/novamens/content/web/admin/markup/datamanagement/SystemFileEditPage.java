package com.novamens.content.web.admin.markup.datamanagement;

import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;


/**
 *  [Breadcrumb]
 *  [] List<File> [edit][remove][download]
 *    
 *  
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 *
 */
public class SystemFileEditPage extends ApplicationPage<Person> {

	private static final long serialVersionUID = 1L;
	
	private final boolean role_service_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
								
	
	public SystemFileEditPage() {
		
		setPageTitle(new ResourceModel("mainmenu.filedit"));
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
	public String getPageHelpKey() {
		return "SystemFileEditPage";
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}

	
	
	
	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder ();
		str.append("<p><b>Service Admin</b> can access this Page. ");
		str.append("You need <b>Service Admin</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		return str.toString();
	}

	/**
	 * Service Admin
	 */
	@Override
	protected boolean hasPermissions() {
		return (getSecurityService().isRoot() || role_service_admin)  && isDomainKbee();
	}
 
	private com.novamens.service.SecurityService getSecurityService() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class);
	}
	
	private void addComponents() {
		if (hasPermissions())
			add(new SystemEditFilePanel("info-panel"));
		else
			add(new ErrorPanel("info-panel", "Not authorized", ""));
			
	}


}
