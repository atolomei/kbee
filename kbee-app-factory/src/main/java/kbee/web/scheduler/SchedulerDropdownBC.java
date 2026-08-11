package kbee.web.scheduler;


import kbee.web.datamanagement.SchedulerCronJobsBC;
import kbee.web.datamanagement.SchedulerRequestBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SchedulerBC;

public class SchedulerDropdownBC extends DropDownMenuBC<Void> {
	private static final long serialVersionUID = 1L;

	public   SchedulerDropdownBC() {
		addElement(new SchedulerBC(), true);
		addElement(new SchedulerBC());
		addElement(new SchedulerRequestBC());
		addElement(new SchedulerCronJobsBC());
	}
}
