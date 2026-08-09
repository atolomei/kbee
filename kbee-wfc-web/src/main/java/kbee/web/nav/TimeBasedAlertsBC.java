package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class TimeBasedAlertsBC extends BCElement {
			
	
	public TimeBasedAlertsBC() {
		super("time-based-alerts");
	}
	
	@Override
	public void onClick() {
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-management-action-rules-page"));
	    // ActionRulesPage.class);
	}
	
}


