package com.novamens.content.web.base.page.component;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class AbstractKbeeHeader extends Panel {

	private static final long serialVersionUID = -7794572837305194718L;

	public AbstractKbeeHeader(String id) {
		super(id);		
		add(new Label("username", getSessionUser()!=null?getSessionUser().getFirstLastName():"guest user"));
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
