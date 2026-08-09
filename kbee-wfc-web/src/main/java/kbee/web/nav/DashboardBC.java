package kbee.web.nav;



import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class DashboardBC extends BCElement {
			
	private static final long serialVersionUID = 1L;

	public DashboardBC() {
		super("bc.dashboard");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.WorkflowDashbaardPage));
	}
}
