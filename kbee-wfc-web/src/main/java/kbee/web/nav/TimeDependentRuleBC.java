package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class TimeDependentRuleBC extends BCElement {

	private static final long serialVersionUID = 1L;

	public TimeDependentRuleBC() {
		super("bc.actionrules");
	}
	
	@Override
	public void onClick() {
		// setResponsePage(new ActionRulesPage());
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-management-actionrules-page"));

	}
	
}
