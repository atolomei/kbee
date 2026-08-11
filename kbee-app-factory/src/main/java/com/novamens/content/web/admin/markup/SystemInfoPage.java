package com.novamens.content.web.admin.markup;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.systeminfo.FactorySystemInfoDropdownBC;

public class SystemInfoPage extends ApplicationPage<Person> implements FactoryPage {

	private static final long serialVersionUID = 1L;

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	public SystemInfoPage() {
		
		setPageTitle(new ResourceModel("mainmenu.systeminfo"));
		Person person = getPerson();
		if (person!=null && hasPermissions()) {
			setTopNavigation(new GlobalNavigationBar<Person>("navigation"));
			setTopNavigation(getMainTopbar());    
			setMenu(getMainLaternalMenu());       
			setModel(new ObjectModel<Person>(person));
			addComponents();
		}
		else {
			add(new ErrorPanel("editor", "person not found", ""));
		}
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.INFO;
	}
	
	protected Panel getBreadcrumbPanel() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
		bc.addElement( new HomeBC());
		bc.addElement( new FactorySystemInfoDropdownBC());
		bc.addElement(new BCElement(getPageTitle()));
		return bc;
	}
	
		
	@Override
	public boolean hasPermissions() {
		return isDomainKbee() || is_root;  
	}
	
	private void addComponents() {
		
		
		PageContentHeaderPanel<Domain> panel=new PageContentHeaderPanel<Domain>(null);
		setPageTitle(new Model<String>("info"));
		panel.setTitle(new Model<String>("info"));
		panel.setBreadcrumbPanel(getBreadcrumbPanel());
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);
		
		add(new SystemInfoPanel("editor"));
	}
}


/**
 * dashboard
 * 
 */