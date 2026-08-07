package com.novamens.kbee.scheduler;

import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.AbstractServiceRequest;


/**
 * 
 * <p>Wrapper to be used to send Cron jobs be to executed immediately</p>
 * @see {@link SchedulerRequestExecutePanel} 
 *
 */
public class CronServiceRequestWrapper extends AbstractServiceRequest {

	private static final long serialVersionUID = 1L;
	
	final AbstractCronJobRequest src;
	
	public CronServiceRequestWrapper(AbstractCronJobRequest src) {
		this.src=src;
		setParameters(src.getParameters());
	}
	
	@Override
	public void execute() {
		src.execute();
	}
	
	final public AbstractCronJobRequest getAbstractCronJobRequest() {
		return src;
	}

	 
}

