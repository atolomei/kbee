package com.novamens.scheduler;

import com.novamens.dom.Domain;
import com.novamens.service.SystemService;

import java.time.OffsetDateTime;
import java.util.List;


public interface CronSchedulerService extends SystemService {

    public List<AbstractCronJobRequest> getCronJobs();
    public List<AbstractCronJobRequest> getCronJobs(Domain domain);
    
    public void updateCronLastExecution(Long id, OffsetDateTime date);
    public void saveCronJob(AbstractCronJobRequest job);
    public void deleteCronJob(AbstractCronJobRequest job);
    
}
