package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class RetentionPolicyBC extends BCElement {

	public RetentionPolicyBC() {
		super("bc.retentionpolicy");
	}
	
	@Override
	public void onClick() {

		// setResponsePage(new ActionRulesPage());
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-management-actionrules-page"));
	}
}
