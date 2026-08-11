package kbee.web.datamanagement;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.scheduler.SchedulerCronJobsPage;
import kbee.web.service.ApplicationSiteMapService;

public class SchedulerRequestBC extends BCElement {
			
	
	public SchedulerRequestBC() {
		super("execute-request");
	}
	
	@Override
	public void onClick() {
	    setResponsePage( new SchedulerRequestPage());
	}
}
