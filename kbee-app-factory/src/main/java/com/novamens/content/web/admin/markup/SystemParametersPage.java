package com.novamens.content.web.admin.markup;


import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;
			
public class SystemParametersPage extends ApplicationPage<Person> implements FactoryPage {

	private static final long serialVersionUID = 1L;

	public SystemParametersPage() {
	} 
	
	@Override
	public void onInitialize() {
			super.onInitialize();

			setPageTitle(new ResourceModel("mainmenu.systeminfo"));
			Person person = getPerson();
			
			if (person!=null) {
				setTopNavigation(getMainTopbar());  
				setMenu(getMainLaternalMenu());     
				setModel(new ObjectModel<Person>(person));
				if (isDomainKbee())
						addComponents();
				else
					add(new InvisiblePanel("info-panel"));
			}
			else {
				add(new ErrorPanel("info-panel", "person not found", ""));
			}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.INFO;
	}
	
	
	
	@Override
	public boolean hasPermissions() {
		return isDomainKbee() ;  
	}
	
	private void addComponents() {
		add(new SystemParametersPanel("info-panel"));
	}


}
