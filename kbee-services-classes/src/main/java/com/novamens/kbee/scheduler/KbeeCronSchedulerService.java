package com.novamens.kbee.scheduler;

import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronSchedulerService;
import com.novamens.scheduler.SchedulerException;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

public class KbeeCronSchedulerService implements CronSchedulerService, EventListener  {
    
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeCronSchedulerService.class.getName());


	public List<AbstractCronJobRequest> getCronJobs() {
        List<AbstractCronJobRequest> cronJobRequests=null;
        try {
            cronJobRequests = getCronJobDao().getCronJobRequests();
        } catch (SchedulerException e) {
            logger.error(e);
        }
        return cronJobRequests;
    }


    public List<AbstractCronJobRequest> getCronJobs(Domain domain) {
        List<AbstractCronJobRequest> cronJobRequests=null;
        try {
            cronJobRequests = getCronJobDao().getCronJobRequests(domain);
        } catch (SchedulerException e) {
            logger.error(e);
        }
        return cronJobRequests;
    }
  
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateCronLastExecution(Long id, OffsetDateTime date) {
        try {
            getCronJobDao().updateCronLastExecution(id, date);
        } catch (SchedulerException e) {
            logger.error(e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveCronJob(AbstractCronJobRequest job) {
        try {
        	
        	if (job.getCronExpression()==null)
        		throw new KbeeRuntimeException ("CronExpression can no be null");
        	
            getCronJobDao().saveRequest(job);
			ServiceLocator.getService(EventService.class).fire(new EvictCronJobsListEvent());
            
            
        } catch (SchedulerException e) {
            logger.error(e);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteCronJob(AbstractCronJobRequest job) {
        try {
            getCronJobDao().deleteRequest(job);
			ServiceLocator.getService(EventService.class).fire(new EvictCronJobsListEvent());

       } catch (SchedulerException e) {
          logger.error(e);
      }
    }

    protected CronJobDao getCronJobDao() {
        return (CronJobDao) ServiceLocator.getService(BeansService.class).getBean("CronJobDao");
    }

	@Override
	public boolean listen(Event event) {
		return event instanceof EvictCronJobsListEvent;
	}

	@Override
	public void onEvent(Event event) {
		logger.debug(event.toString());
		
	}

}
