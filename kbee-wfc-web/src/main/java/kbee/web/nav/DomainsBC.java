package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class DomainsBC extends BCElement {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DomainsBC() {
		super("bc.domains");
	}
	
	/**
	 * 
	 * new DomainsPage()
	 */
	@Override
	public void onClick() {
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-domains-page"));
	}
}
