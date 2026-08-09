package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;
				
public class SchedulerBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public SchedulerBC () {
		super("bc.scheduler");
	}
	
	@Override
	public void onClick() {
		// setResponsePage(new SystemSchedulerMonitorPage());
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-scheduler-monitor-page"));


	}
}
