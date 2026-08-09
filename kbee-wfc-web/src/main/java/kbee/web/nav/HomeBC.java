package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class HomeBC extends BCElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HomeBC() {
		super("home");
	}
	
	public void onClick() {
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage));
	}
	
}
