package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class UserAlertsBC extends BCElement {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public  UserAlertsBC() {
		super("bc.user-alerts");
	}
	
	@Override
	public void onClick() {
		// 
		// 
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-notifications-page"));
	}


}
