package kbee.web.datamanagement;

import com.novamens.wicket.util.BCElement;

import kbee.web.scheduler.SchedulerCronJobsPage;

public class SchedulerCronJobsBC extends BCElement {

	
	
	
public SchedulerCronJobsBC() {
	super("cronjobs");
}

@Override
public void onClick() {
    setResponsePage( new SchedulerCronJobsPage());
}
}
