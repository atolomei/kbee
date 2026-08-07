package com.novamens.scheduler;


import java.io.Serializable;
import java.util.Iterator;


/**
 * <p>CronJobs are {@link ServiceRequest} that 
 * the {@link SchedulerService} 
 * executes regularly according to their {@link CronExpressionJ8}
 * 
 *
 */
public interface CronJobsList {
	public AbstractCronJobRequest getFirst();
	public AbstractCronJobRequest pollFirst();
	public Iterator<AbstractCronJobRequest> iterator();
	public Serializable add(AbstractCronJobRequest job);
	
	public void reset();
}
