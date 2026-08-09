package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class EmailNotificationsBC extends BCElement {
		
	private static final long serialVersionUID = 1L;

	public EmailNotificationsBC() {
		super("bc.enoti");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-management-enoti-rule-page"));
		
	}

	

	
}
