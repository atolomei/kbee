package com.novamens.content.web.admin.markup.datamanagement;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.datamanagement.FactoryDataManagementDropdownBC;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.scheduler.SchedulerDropdownBC;

public class SystemSchedulerMonitorPage extends ApplicationPage<Person> implements FactoryPage {
	
	private static final long serialVersionUID = 1L;
					
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin			= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()));
	final boolean is_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_operations				= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId()));	
									
	public SystemSchedulerMonitorPage() {
		Person person = getPerson();
		setPageTitle(new ResourceModel("mainmenu.datamanagement"));
		if (person!=null) {
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu()); 
			setModel(new ObjectModel<Person>(person));
			addComponents(); 
		}
		else {
			add(new ErrorPanel("editor", "person not found", ""));
		}
	}

	
	protected Panel getBreacrumbPanel() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
		bc.addElement( new HomeBC());
		bc.addElement( new FactoryDataManagementDropdownBC());
		bc.addElement( new SchedulerDropdownBC());
		bc.addElement(new BCElement(new StringResourceModel("scheduler", SystemSchedulerMonitorPage.this, null)));
		return bc;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(new StringResourceModel("scheduler", this, null));
		
	    PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>();
		panel.setTitle(new StringResourceModel("scheduler", this, null));
		panel.setBreadcrumbPanel(getBreacrumbPanel());
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.domains", this, null).getObject()));
		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		panel.setSearchPanel(getSearchPanel());
		setPageContentHeader(panel);
		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.DATA_MANAGEMENT;
	}

	@Override
	protected boolean hasPermissions() {
		return isDomainKbee() && (is_domain_admin || is_root || is_service || is_factory_admin || is_operations || is_support);
	}
 
	private void addComponents() {
		if (hasPermissions())
			add(new SystemSchedulerMonitorPanel("editor"));
		else 
			add(new ErrorPanel("editor", "Not authorized", ""));
	}
}
