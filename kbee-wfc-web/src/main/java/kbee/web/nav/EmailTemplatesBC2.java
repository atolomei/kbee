package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class EmailTemplatesBC2 extends BCElement {
					
	private static final long serialVersionUID = 1L;

	public EmailTemplatesBC2() {
		super("bc.emailtemplates");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-emailtemplates-page"));
	}
	
}
