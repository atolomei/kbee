package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class SitesBC extends BCElement {
	
	private static final long serialVersionUID = 1L;

	public SitesBC() {
		super("sites");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("portal-sites-page"));
	}

}
