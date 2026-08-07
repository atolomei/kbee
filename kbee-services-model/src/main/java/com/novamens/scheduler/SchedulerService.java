package com.novamens.scheduler;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.service.SystemService;


/**
 * <p> 
 * The Scheduler (part of he System Services layer), is a multithreaded  job queue and scheduler that executes jobs 
   as part of the same Atomic Transaction with other critical tasks (content versioning, workflow task, ...). 

The main purpose of the Scheduler is to support a highly asynchronous operation of the application.
The Scheduler maintains a queue of jobs, which are executed by its worker threads.
 
 * 
 * The queue uses the Database to store jobs.
 * The main reason why we use the Database is because we wanted the Job Queue to be integreated to the DB transaction that calls it. 

 *  It supports 3 priorites (System, High, Low), each with their own thread pool.
 *  
 * Jobs must be a Subclass of {@link ServiceRequest}
 * It also supports Cron jobs {@link AbstractCronJobRequest}
 * </p>
 * 

<p>RPDD Scheduler Jobs are classes that implement the ServiceRequest Interface and must be Serializable

The Scheduler supports the execution of jobs regularly defined by a CronExpression (example: a process that cleans up disk every day at 3 AM)
Cron jobs must be subclasses of AbstractCronJobRequest</p>

<p>
Examples:
<br />
When a file is uploaded, the Content Service from the Content Management layer creates a job to asynchronously index the content of the file (and allow to complet the db transaction quickly).
When a user edits a Role, the Security Service creates a job to asynchronously index part of the database.
When a user sends a file by email, the Web App component creates a SendEmail job and returns.
<br />
There are 2 persistent priority queues, 
High Priority (HP) and 
Low Priority (LP). 
<br />
The Scheduler manages  a thread pool -normally 20-40 threads- that take jobs from the queues and execute them.
If a job fails to execute it is queued back 3 times in total before being marked as "error".
</p>


Example: <br/>

 Scheduler assumes the caller has a DB transaction, therefore this code must by contained by a DB transaction,
 EmailSendServiceRequest is a ServiceRequest that can send an Email, taking the info from the EmailData instance
<br/>

{@code EmailData data=new EmailData(from, to, subject, str, null, context_info, null);}
<br/> 
{@code 
	           EmailSendServiceRequest req = new EmailSendServiceRequest(emaildata, domain); 
 	           ServiceLocator.getService(SchedulerService.class).enqueue(req); 
	           

}
 *
 */
public interface SchedulerService extends SystemService {
	
	public static final int LOW_PRIORITY 	= 2;
	public static final int HIGH_PRIORITY 	= 1;
	
	public static final int STANDARD_PROCESSING_COST = 1;
	
	public void start();
	
	public Serializable enqueue(ServiceRequest request) throws SchedulerException;
	
	public boolean isEmpty() throws SchedulerException;
	public int getQueueSize() throws SchedulerException;
	public int getErrorQueueSize() throws SchedulerException;
	
	public boolean started();
	
	
	//public void restart(boolean force_stop_batches) throws SchedulerException;
	
	public void removePhantomRequest();

	public double getOneMinuteInputRateHp();
	public double getFifteenMinuteInputRateHp();
	public double getFiveMinuteInputRateHp();
	
	public double getOneMinuteInputRateLp();
	public double getFifteenMinuteInputRateLp();
	public double getFiveMinuteInputRateLp();
	
	public double getOneMinuteThroughPutHP();
	public double getFiveMinuteThroughPutHP();
	public double getFifteenMinuteThroughPutHP();

	public double getOneMinuteThroughPutLP();
	public double getFiveMinuteThroughPutLP();
	public double getFifteenMinuteThroughPutLP();
	
	public double getMeanHPIn();  
	public double getMeanLPIn();
											
	public double getMeanHPOut();
	public double getMeanLPOut();

	public SchedulerQueue<ServiceRequest> getQueue();
	
	public Thread getThread();

	public int getPriority();
	
	public int getTotalInBatches();  // Total Requests in Batches
	public int getTotalBatches();    // Total Batches in the System
	
	public List<AbstractCronJobRequest> getCronJobs();

	public List<String> getDispatchersInfo();
	
	/**

	 * If there are more than X  
	 * requests waiting for more than Y minutes to be taken in the queue,
	 * it means the Scheduler main thread is not working.
	 * 
	 *  X  = scheduler.error.size (default 20)
	 *  Y = scheduler.error.minutes.waiting (default  10)
	 *  
	 * @return
	 * @throws SchedulerException
	 */
	public String getStatus() throws SchedulerException;
	
	/**
	 * This method is aimed to inform the status of all the
	 * Requests in the Scheduler.
	 * 
	 * They are either:
	 * 
	 * Wating  (for a Thread to execute)
	 * Executing 
	 * Terminated (but the Batch is not yet done)
	 * 
	 * @return
	 */
	public Map<String, List<String>> getRequestStatus();
	
	/**
	 * 
	 * This is useful to check if the Dispatchers' threads are hang
	 * and no more threads can be executed.
	 * 
	 * @return
	 */
	public Map<String, String> getDispatchersStatus();

	public Map<String, String> getBatchStatus();

	public void addRequestToken(Serializable key);
	public void removeRequestToken(Serializable key);
	public boolean containsRequestToken(Serializable key);
	public void processCronJobById(Serializable id);
	public OffsetDateTime getStartDateTime();
	public Map<String, String> getConfigurableParameters();
	
	/*
	 * Id, Description
	 */
	
	public Map<String, String> getRunningRequestsStr();
	public void restart() throws SchedulerException;

	OffsetDateTime lastRestart();

	public List<Class<? extends ServiceRequest>> getAllServiceRequestClasses();

	void stopAllThreads();

	
}
