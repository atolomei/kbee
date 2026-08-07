package com.novamens.kbee.scheduler;

import java.util.Collection;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerException;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;
import com.novamens.util.KbeeRuntimeException;

public class KbeeSchedulerServiceFactory extends AbstractServiceFactory<SystemService> {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	
	private KbeeSchedulerService service;
	private boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isAssignableFrom(KbeeSchedulerService.class);
	}
	
	
	/**
	 * <p>Add System Cron Jobs</p>
	 */
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		if (!service.started())
			synchronized (this) {
				if (!started) {
					service.start();
				try {
					int n = 0;
					for (AbstractCronJobRequest job : getBeanCronJobs()) {
						job.setId(-1 * Math.abs(Long.valueOf(Double.valueOf(Math.random()*100000000).longValue())));
						service.enqueue(job);
						n++;
						logger.debug(job.getName() +". " +job.getCronExpression());
					}
					logger.debug("System ServiceRequest CronJobs -> " + String.valueOf(n) + " item" + (n==1?".":"s."));
				}
				catch (SchedulerException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
					started = true;
				}	
			}
		return (S)service;
	}
	
	public void setService(KbeeSchedulerService service) {
		this.service = service;
	}
	
	protected Collection<AbstractCronJobRequest> getBeanCronJobs() {
		Map<String, AbstractCronJobRequest> jobs = ServiceLocator.getService(BeansService.class).getBeansOfType(AbstractCronJobRequest.class);
		return jobs.values();
	}
}
