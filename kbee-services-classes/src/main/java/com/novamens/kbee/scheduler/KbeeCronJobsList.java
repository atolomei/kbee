package com.novamens.kbee.scheduler;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.novamens.beans.BeansService;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronJobsList;
import com.novamens.scheduler.SchedulerException;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * delete from kb_cronjob;
 * 
 * <p>
 * -- Ping
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'PingServiceRequest', 'Ping. System Parameters: ping.enabled = yes/no|  ping.notify = yes/no | ping.email = email to send Ping error.', '15 * * * * *', 'com.novamens.kbee.content.command.PingServiceRequest');
 * <p>
 * -- CLEAN SolR indexes
 * -- Delete old Events
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'SQLCronJobRequest clean Scheduler',  'delete from scheduler where time < (now() - INTERVAL 15 days)::timestamp',              '0 15 8 ? * SUN', 'com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest',      'delete from scheduler where time < (now() - INTERVAL ''15 days'')\:\:timestamp');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'SQLCronJobRequest clean log event',  'Clean Log Events. We keep just the last 2 years PostgreSQL',                            '0 17 8 ? * SUN', 'com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest',      'delete from logevent where event_time < (now() - INTERVAL ''2 year'')\:\:timestamp');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'SQLCronJobRequest clean log emails', 'delete from kb_sendemailevent where event_time < (now() - INTERVAL 1 year)::timestamp', '0 20 8 ? * SUN', 'com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest',      'delete from kb_sendemailevent where event_time < (now() - INTERVAL ''1 year'')\:\:timestamp');
 * <p>
 * -- Log usage stat
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'LogUsageServiceRequest', 'Logs daily usage Hard Disk, Contents, Users, Resources for every domain', '0 32 23 * * *', 'com.novamens.kbee.logging.usage.LogUsageServiceRequest');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'LogApiUsageServiceRequest ', 'Log API Usage last day', '0 13 1 * * *', 'com.novamens.kbee.logging.usage.LogApiUsageServiceRequest');
 * <p>
 * <p>
 * -- Clean up (directories, worknotes, recyclebin, scheduler, log event, email event)
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values            ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'WorkNotesNotificationsCleanUpServiceRequest', 'Deletes Work Note Notifications sent X months ago. By default X= 1 (work-notes-notification-retention-months)',    '0 30 4 ? * SUN', 'com.novamens.kbee.content.notification.WorkNotesNotificationsCleanUpServiceRequest');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values            ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'CleanUpExportDirRequest',                     ' ',                                                                                                                                    '0 15 0 * * *', 'com.novamens.kbee.content.service.datamanagement.CleanUpExportDirRequest');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values            ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'RecycleBinCleanUpServiceRequest',             'Deletes old Contents in the Recycle Bin',                                                                                      '0 20 3 * * *', 'com.novamens.kbee.content.command.RecycleBinCleanUpServiceRequest');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'ReprocessAPIRequestsCronJobRequest',          '(ReprocessCommand) every Sunday at 0:35:00, size is the max elements to process ','0 35 0 ? * SUN', 'com.novamens.kbee.content.webapi.command.ReprocessAPIRequestsCronJobRequest','8000');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values            ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'RemoveOldExportsRequest',                     'RemoveOldExportsRequest','0 45 3 * * *', 'com.novamens.content.web.admin.markup2.datamanagement.RemoveOldExportsRequest');
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values            ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'RestartCronJobRequest',                       'Restart the application at 3:35 AM on the 11th day of every month',   '0 35 3 11 * ? ', 'com.novamens.kbee.content.command.RestartCronJobRequest');
 * <p>
 * delete from kb_cronjob where name like 'WorkNotes%'  or name like 'CleanUpExportDirRequest%' or name like 'RemoveOldExportsRequest%' or name like 'SQLCronJobRequest%' or name like 'ContentPublishNotificationsCleanUpServiceRequest%';
 * 
 * 
 * 
 *  		delete from kb_cronjob where name='Cancel Idle Transaction';
			insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) 
			values 
			(
			(select nextval('objectid_sequence')), 
			(select id from users where username='root@kbee'), 
			'Cancel Idle Transaction',  
		    'Cancel TRX that have been idle for more than 2.5h',
			'38 50 * * * *', 
			'com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest', 
			'SELECT pg_terminate_backend(pid) from (select pid from pg_stat_activity where pid <> pg_backend_pid() and  state  like ''idle in transaction%''   and now()- xact_start > ''150 minute''\:\:interval) AS ACT');

 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */


public class KbeeCronJobsList implements CronJobsList, EventListener {
	
    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	static private kbee.util.logging.Logger blogger = kbee.util.logging.Logger.getLogger(KbeeCronJobsList .class.getName());

    private TreeSet<AbstractCronJobRequest> jobs = null;

    private class CronJobComparator implements Comparator<AbstractCronJobRequest> {
        @Override
        public int compare(AbstractCronJobRequest a, AbstractCronJobRequest b) {
            try {
            	
                if (a.getTime() == null && b.getTime() == null) return 0;
                if (a.getTime() == null) return -1;
                if (b.getTime() == null) return 1;
                
                logger.debug(a.getName() +" -> " + a.getTime().toString() +" " + b.getName()+" -> "+b.getTime());
                
                return a.getTime().isBefore(b.getTime()) ? -1 : 1;
                
            } catch (Exception e) {
                logger.error(e);
                blogger.error(e);                
                return 0;
            }
        }
    }

    
    public Serializable add(AbstractCronJobRequest job) {
        getJobs().add(job);
        return job.getId();
    }

    public AbstractCronJobRequest getFirst() {
       return !getJobs().isEmpty() ? getJobs().first() : null;
    }

    public AbstractCronJobRequest pollFirst() {
        AbstractCronJobRequest job = getJobs().pollFirst();
        AbstractCronJobRequest nextjob = job.clone();
        nextjob.setTime(job.getNextTime());
        getJobs().add(nextjob);
        return job;
    }

    public Iterator<AbstractCronJobRequest> iterator() {
        return getJobs().iterator();
    }

    
    public boolean listen(Event event) {
      // return event instanceof EvictCacheServiceEvent;
    	return false;
    }
    
    public void onEvent(Event event) {
        //Assert.isInstanceOf(EvictCacheServiceEvent.class, event);
        reset();
    }


    public void reset() {
    	jobs = null;
    }
    
    protected TreeSet<AbstractCronJobRequest> getJobs() {
        if (jobs == null) {
            synchronized (this) {
                this.jobs = loadJobs();                
            }
        }
        return jobs;
    }

    protected TreeSet<AbstractCronJobRequest> loadJobs() {
        try {
        	
            logger.debug("Start loading jobs");
            blogger.debug("Start loading jobs");
            
            List<AbstractCronJobRequest> list = getCronJobDao().getCronJobRequests();
            TreeSet<AbstractCronJobRequest> jobs = new TreeSet<AbstractCronJobRequest>(new CronJobComparator());
            for (AbstractCronJobRequest job : list) {
                if(job.isEnabled()) {
                    jobs.add(job);
                    blogger.debug("Adding -> " + job.toString());
                    logger.debug("Adding -> " + job.toString());
                }
            }
            return jobs;
            
        } catch (SchedulerException e) {
            logger.error(e);
            blogger.error(e);
            return null;
        }
    }

    protected CronJobDao getCronJobDao() {
        return (CronJobDao) ServiceLocator.getService(BeansService.class).getBean("CronJobDao");
    }
    
    protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
