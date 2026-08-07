package com.novamens.kbee.scheduler;


import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

				
public class ClearPhantomRequest extends AbstractCronJobRequest {
				
	private static final long serialVersionUID = 4740049486472648474L;
	
	 public ClearPhantomRequest() {
	 	setName("Clear Phantom Requests");
	 }

	/**
	 * The trx is executed inside the Scheduler worker thread 
	 */
	@Override
	public void execute() {
		SchedulerService service = ServiceLocator.getService(SchedulerService.class);
		  service.removePhantomRequest();
	}

	public void setCronExpression(String expression) {
		super.setCronExpression(new CronExpressionJ8(expression));
	}

}
