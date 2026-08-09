package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class EmailTemplatesBC extends BCElement {
	private static final long serialVersionUID = 1L;


	public EmailTemplatesBC() {
		super("bc.emailtemplates");
	}
	
	@Override
	public void onClick() {
		// TODO BC
		// setResponsePage(new EmailTemplatesPage());
																							
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-emailtemplates-page"));
		
	}
	

	
	
}
