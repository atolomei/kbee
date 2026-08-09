package com.novamens.kbee.wicket.markup.html.console.panel;


import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

/**
 * <p>All Right Panels should be subclasses of this one.</p> 
 *
 */
public abstract class ConsoleSidePanel extends KBPanel {

	private static final long serialVersionUID = 1L;
	
	
	private String consoleName;
	private String consoleDisplayName;

	
	public ConsoleSidePanel(String id) {
		super(id);

	}

	public void reload(AjaxRequestTarget target) {
		target.add(this);
	}
	
	
	public String getConsoleName() {
		return this.consoleName;
	}
	
	public void setConsoleName(String consoleName) {
		this.consoleName=consoleName;
	}
	
	
	public String getConsoleDisplayName() {
		return this.consoleDisplayName;
	}
	
	
	public void setConsoleDisplayName(String consoleName) {
		this.consoleDisplayName=consoleName;
	}
	
	public abstract void onClose(AjaxRequestTarget target);
	

	protected User getSessionUser() {
		return (User) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	

}
