package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class SystemDataManagementBC extends BCElement {
				
	private static final long serialVersionUID = 1L;

	public SystemDataManagementBC() {
		super("bc.systemdm");
	}
	
	@Override
	public void onClick() {
		// setResponsePage(new SystemDataManagementPage());
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-system-data-management-page"));
	}
}
