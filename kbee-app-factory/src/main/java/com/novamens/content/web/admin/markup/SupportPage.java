package com.novamens.content.web.admin.markup;

import org.apache.wicket.model.ResourceModel;


import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationPage;

public class SupportPage extends ApplicationPage<Person> {

	private static final long serialVersionUID = 1L;

	public SupportPage() {
		
		setPageTitle(new ResourceModel("mainmenu.support"));
		Person person = getPerson();
		
		setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
		setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));

		
		if (person!=null) {
			setModel(new ObjectModel<Person>(person));
			if (isDomainKbee())
					addComponents();
			else
				add(new InvisiblePanel("info-panel"));
		}
		else {
			
			add(new ErrorPanel("info-panel", "person not found!", ""));
		}
	}
	
	 
	
	private void addComponents() {
		add(new SupportPanel("support-panel"));
	}
}
