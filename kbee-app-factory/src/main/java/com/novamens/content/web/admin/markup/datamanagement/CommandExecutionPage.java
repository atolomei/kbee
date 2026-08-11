package com.novamens.content.web.admin.markup.datamanagement;

import com.novamens.content.command.Command;

import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.dom.Domain;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationPage;

public class CommandExecutionPage  extends ApplicationPage<Void> {
	private static final long serialVersionUID = 1L;
	
	public CommandExecutionPage(Command command) {
		
		setTopNavigation(getMainTopbar());       // setNavigation(new GlobalNavigationBar<Person>("navigation"));
		setMenu(getMainLaternalMenu());       // setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));

		
		
		add(new CommandExecutionPanel(command));
	}
}
