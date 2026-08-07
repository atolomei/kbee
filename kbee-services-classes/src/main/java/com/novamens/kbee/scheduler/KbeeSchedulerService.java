package com.novamens.kbee.scheduler;


import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.Synchronization;

import com.novamens.scheduler.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.novamens.cache.SelfExpiringHashMap;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.metrics.KbeeSystemMetricsService;
import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.PropertiesFactory;

/**
 *  <p>Queue bufferSize:
 *  
 *	max_queue_size  	=  Integer.valueOf(20); 
 *	max_min_waiting     =  Integer.valueOf(10);
 * 
 * Each batch accesses it
 * 
 * 1. KbeeSchedulerService
 * 2. Dispatcher Worker
 * </p>
 * 
 * @see {@link ShedulerService}
 */
public class KbeeSchedulerService implements SchedulerService,  EventListener, Runnable {

	static private kbee.util.logging.Logger startupLogger = kbee.util.logging.Logger.getLogger("StartupLogger"); 
	
	static private Logger logger_tokens = LogManager.getLogger("tokenslogger");

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	private static kbee.util.logging.Logger blogger = kbee.util.logging.Logger.getLogger(KbeeSchedulerService.class.getName());
								
	static private int BATCH_SIZE 		= 18; 				// units of work. Normally, fast tasks take 1 unit, and heavier tasks more.
					
	static private final int IDEAL_BUFFER_SIZE  = BATCH_SIZE * 3; 	// ideal number of requests to load in each cycle
	private final static long CONSERVATIVE_SIESTA_SECS =  6; 	    // secs
	
	static private final int MINUTES_30 = 1000 * 60 * 30;
	
	static private final String	CRON_JOBS_LOCK	= "SCHEDULER_CRON_JOBS";
	
	static int MAX_ALLOWED_IN_RAM = 80000;
	static int fixedRestMs=2000;
	
	static {
		try {
				MAX_ALLOWED_IN_RAM = Integer.valueOf(PropertiesFactory.getInstance("kbee").getProperties().getProperty("scheduler.maxinram", "80000").toLowerCase().trim()).intValue();
		} catch (Exception e) {
				logger.error(e);
				blogger.error(e);
				MAX_ALLOWED_IN_RAM = 80000;
		}
		try {
			fixedRestMs = Integer.valueOf(PropertiesFactory.getInstance("kbee").getProperties().getProperty("scheduler.restms", "2000").toLowerCase().trim()).intValue();
		} catch (Exception e) {
				logger.error(e);
				blogger.error(e);
				fixedRestMs = 2000;
		}
		

		try {
			BATCH_SIZE = Integer.valueOf(PropertiesFactory.getInstance("kbee").getProperties().getProperty("scheduler.batch-size", "18").toLowerCase().trim()).intValue();
		} catch (Exception e) {
				logger.error(e);
				blogger.error(e);
				BATCH_SIZE = 18;
		}
	}
	
	// Synchronization
	public abstract class ThreadSynchronization implements Synchronization {
		private String transactionId; 
		public ThreadSynchronization(org.hibernate.Transaction transaction) {
			this.transactionId = String.valueOf(transaction.hashCode());
		}
		public String getTransactionId() {
			return transactionId;
		}
		public boolean inTransaction(org.hibernate.Transaction transaction) {
			return getTransactionId().equals(String.valueOf(transaction.hashCode()));	
		}
	}
	
	
	private SessionFactory sessionFactory;

	private int maxInRound = IDEAL_BUFFER_SIZE;
	private Thread thread;
	
	
	private Instant last_queue_processing 	= Instant.now();
	private OffsetDateTime started 			= OffsetDateTime.now();
	
	private SchedulerQueue<ServiceRequest> activationQueue;
	
	private Map<Thread, Synchronization> transactions 	= Collections.synchronizedMap(new HashMap<Thread, Synchronization>());
	private List<Batch> 				batches 		= Collections.synchronizedList(new ArrayList<Batch>()); /*** The List is read/written from the main Scheduler Thread and from each of the Batch Thread. The Lock coordinates the iteration over the elements from all those threads. **/
	
	
	private ReentrantLock 				main_loop_lock 	= new ReentrantLock();
	private ReadWriteLock 				batches_lock 	= new ReentrantReadWriteLock();
	
	private Map<Integer, Dispatcher> 	dispatchers 	= Collections.synchronizedMap(new HashMap<Integer, Dispatcher>());
	private SelfExpiringHashMap<Serializable, Serializable> request_token = new SelfExpiringHashMap<Serializable, Serializable>(MINUTES_30); /** Token service */

	private CronJobsList cronJobs = null;
	
	private int priority    	= Thread.currentThread().getPriority();
	private int batch_counter 	= 0;
	
	private AtomicInteger incoming 			= new AtomicInteger(0);
	private AtomicBoolean maxRoundCapacity  = new AtomicBoolean(false);

	OffsetDateTime last_restart = OffsetDateTime.now();
	
	private volatile boolean sleeping		= false; // thread safe
	
	/**  Metrics 	*/
	private Meter metric_requests_hp;    	/** num requests hp in / sec */
	private Meter metric_requests_lp;   	/** num requests lp in / sec  */
 	private Meter metric_throughput_hp;    	/** num requests hp completed / sec */
	private Meter metric_throughput_lp;    	/** num requests lp completed / sec */
	
	private SchedulerStatus status  = SchedulerStatus.STARTING;

	private AtomicBoolean _restart = new AtomicBoolean(false);
	
	@Override
	public void start()  {
		startupLogger.info("Starting Scheduler");
		logger.debug("Starting Scheduler");
		
		this.thread = new Thread(this);
		this.thread.setDaemon(true);
		this.thread.setName("Scheduler");
		this.thread.setPriority(this.getPriority());
		this.thread.start();
	}

	
	/** 
	 * MT
	 * Adds {@link ServiceRequest} to the Scheduler
	 * 
	 * Caller Thread
	 */
	@Override
	public synchronized Serializable enqueue(ServiceRequest request) throws SchedulerException  {
		
		Serializable r_id = null;
		
		if (request.isCronJob()) {  
			if (!(request instanceof AbstractCronJobRequest) || ((AbstractCronJobRequest)request).getCronExpression()==null) {
				logger.error("not subclass of AbstractCronJobRequest or Cron Expression is null -> " + request.toString());
			}	
			else {
				/**
				 *  the ServiceRequest must be already saved in the database or have a unique id
				 *  see {@link KbeeSchedulerServiceFactory}
				 */
				r_id  = getCronJobsList().add((AbstractCronJobRequest)request);
				this.incoming.addAndGet(1);
				logger.debug("Enqueue cronjob -> " + request.toString()); // the id is not valid here
			}
		}
		else {
			r_id =  getQueue().enqueue(request);
			addNotifySynchronization();
			logger.debug("Enqueue -> " + request.toString());
		}
		
		// getMeterHP/LP: counts the rate at which Requests enter the system
		getMeterInput(request.getPriority()).mark();
		
		return r_id;
	}
	
	/** 
	 * <p>Scheduler Thread
	 * Runs inside the Scheduler thread
	 *  When the Reset command arrives
	 *  the main thread stops dispatching batches until the reset is completed.
	 *  </p>
	 */
	@Override
	public void run() {

		if (this.dispatchers==null || this.dispatchers.size()<2)
			throw new KbeeRuntimeException("dispatchers is null or dispatchers size is less than 2");
		
		Transaction transaction = null;
		
		try {
			/** sleeps 18 seconds (Scheduler Thread) to allow other services to start up before starting to process Requests */
			Thread.sleep(18000); 
		} catch (InterruptedException e1) {}
		
		setStatus(SchedulerStatus.RUNNING);
		
		while (isRunning()) {
	
			main_loop_lock.lock();
			
			try {
				
				boolean isTooManyRequests = isTooManyRequests();
				
				if (!isResseting() && !isTooManyRequests) {
					try {
						this.incoming.set(0);					
						transaction = beginTransaction();
						processCronJobs();
						processQueue();
						transaction.commit();
					}
					catch (Throwable e) {
						logger.error(e);
						blogger.error(e);
						transaction.rollback();
					}
				}
				
				if(isTooManyRequests) {
					if(logger.isDebugEnabled()) {
						logger.debug("To Many request(" + this.getTotalInBatches() + ") sleeping fixes time(ms): " + fixedRestMs);
					}
					rest(fixedRestMs);
				} else if (!isWork()) {
					rest();
				}	
			}
			catch (Throwable e) {
				logger.error(e);
				blogger.error(e);
				logger.debug(getCriticalStatus());
				rollback(transaction);
				criticalRest();
			}
			
			finally {
				main_loop_lock.unlock();
			}
			

			try {
				if (_restart.get()) {
					internal_restart();
				}
			} catch (Exception e) {
				logger.error(e);
			}
			
		}
		
		setStatus(SchedulerStatus.ERROR);
		
		logger.debug("end main loop");
	}
	
	
	
	
	
	@Override
	public synchronized void restart()  { 
		_restart  = new AtomicBoolean(true);
		logger.debug("Notifiying Scheduler thread to wake up");
		notifyAll();
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
		}
	}

	
	
	
	/***
	 * 
	 **/ 
	private void internal_restart() throws SchedulerException { 

		long start = System.currentTimeMillis();
		
		main_loop_lock.lock(); 
		
		try {
			
			_restart  = new AtomicBoolean(false);
			
			setStatus(SchedulerStatus.RESETTING);
			
			logger.debug("--------------------------------------------------------------------------------------------------------------");
			logger.debug("Starting to Shutdown the Scheduler");
				
			logger.debug("try to complete or abort all batches");
			clearBuffers(true);
			
			boolean busy = (this.batches.size()>0);
			
			while (busy) {
				try {
					logger.debug("Sleeping 1s to wait for the Queue to complete executing batches");
					Thread.sleep(1000);
					busy = (this.batches.size()>0) && ((System.currentTimeMillis() - start) < 60000);
				} 
				catch (InterruptedException e) {
					logger.error(e);
				}
			}
			
			logger.debug("restart all Dispatchers' threads");
			for (Entry<Integer, Dispatcher> entry: getDispatchers().entrySet())
				entry.getValue().restart(true);
			
			this.transactions 			= Collections.synchronizedMap(new HashMap<Thread, Synchronization>());
			this.batches 				= Collections.synchronizedList(new ArrayList<Batch>()); 

			
			getQueue().restartQueue();
			
			this.request_classes = null;
			
			logger.debug("--------------------------------------------------------------------------------------------------------------");
			logger.debug("Scheduler restarted. total time:  " +  String.valueOf(System.currentTimeMillis()-start) + " ms");
			
			last_restart = OffsetDateTime.now();
		} 
		
		catch (Exception e) {
			logger.error(e, e.getMessage());
			throw(e);
		}
		finally {
			logger.debug("Restart done.");
			logger.debug("--------------------------------------------------------------------------------------------------------------");
			setStatus(SchedulerStatus.RUNNING);
			main_loop_lock.unlock();
		}
	}
	 
	@Override
	public OffsetDateTime lastRestart() {
		return this.last_restart;
	}
	
	
	@Override
	public  OffsetDateTime getStartDateTime() {
		return this.started;
	}

	
	@Override
	public synchronized void removePhantomRequest() {
		try {
			logger.debug("cleaning Queue from existing phantom requests...");
			getQueue().cleanPhantomRequests();
		}
		catch (Exception e) {
			logger.error(e);
			blogger.error(e);
		}
	}

	@Override
	public boolean started() {
		return this.status!=SchedulerStatus.STARTING;		
	}
	
	@Override
	public Thread getThread() {
		return this.thread;
	}

	public void setPriority(int aPriority) {
		this.priority = aPriority;
	}
	
	@Override
	public int getPriority() {
		return this.priority;
	}

	public void setDispatchers(List<Dispatcher> dispatchers) {
		for (Dispatcher dispatcher : dispatchers) 
			getDispatchers().put(dispatcher.getPriority(), dispatcher);
	}
		
	public Map<Integer, Dispatcher> getDispatchers() {
		return this.dispatchers;
	}
	
	@Override
	public int getTotalBatches() {
		return getBatches().size();
	}

	/**
	 * Total Requests currently in RAM
	 */
	@Override
	public int getTotalInBatches()	{
		int total = 0;
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch: getBatches()) 
				total+=batch.getSize();
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
		return total;
	}
	
	@Override
	public List<String> getDispatchersInfo() {
		List<String> list =  new ArrayList<String>();
		for (Entry<Integer, Dispatcher> entry: getDispatchers().entrySet()) 
				list.add(entry.getValue().getInfo());
		return list;
	}

	@Override
	public synchronized String getStatus() throws SchedulerException {
		
		try {
			StringBuilder str = new StringBuilder();
				
			str.append(getQueue().getQueueStatus());
			
			if (getInternalStatus()==SchedulerStatus.ERROR) {
				if (str.length()>0)
					str.append(" | ");
				str.append("Status:ERROR. main loop not running");
			}
			
			if (Instant.now().isAfter(this.last_queue_processing.plusSeconds(CONSERVATIVE_SIESTA_SECS))) {
				if (str.length()>0)
					str.append(" | ");
				str.append(" slept too much; main thread may not be working");
			}
			
			return str.toString();
			
		} catch (Exception e) {
			logger.error(e);
			blogger.error(e);
			return e.getClass().getSimpleName() +" | " + e.getMessage();
		}
			
	}
	
	public void setQueue(SchedulerQueue<ServiceRequest> queue) {
		this.activationQueue = queue;
	}
	
	@Override
	public SchedulerQueue<ServiceRequest> getQueue() {
		return activationQueue;
	}
	
	@Override
	public int getQueueSize() throws SchedulerException {
		return getQueue().getSize();
	}
	
	@Override
	public int getErrorQueueSize() throws SchedulerException {
		return getQueue().getErrorSize();
	}
	
	public void setCronJobs(CronJobsList jobs) {
		this.cronJobs = jobs;
	}
	
	public CronJobsList getCronJobsList() {
		return cronJobs;
	}
	
	@Override
	public List<AbstractCronJobRequest> getCronJobs() {
		List<AbstractCronJobRequest> jobs = new ArrayList<AbstractCronJobRequest>();
		Iterator<AbstractCronJobRequest> jobsiterator = getCronJobsList().iterator();
		while (jobsiterator.hasNext()) {
			jobs.add(jobsiterator.next());
		}
		return jobs;
	}
	
	@Override
	public boolean isEmpty() throws SchedulerException {
		if (!getQueue().isEmpty())
			return false;
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch: getBatches()) { 
				if (batch.isRunnnig())
					return false;
				if (batch.getSize()>0)
					return false;
			}
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
		return true;
	}
	
	/**
	 * Map <Batch, List of the Batch's Requests> 
	 */
	@Override
	public Map<String, List<String>> getRequestStatus() {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch: getBatches()) 
				map.put(batch.getName(), batch.getServiceRequestStatus());
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
		return map;		
	}
	
	/**
	 * Status of the two Dispatchers (High, Low)
	 * 
	 * This method is useful to know if all the threads of the
	 * Dispatcher are taken for ever. ie. "Starvation"
	 */
	@Override
	public Map<String, String> getDispatchersStatus() {
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<Integer, Dispatcher> entry: getDispatchers().entrySet()) 
			map.put( String.valueOf(entry.getValue().getPriority()), entry.getValue().getStatus());
		return map;
	}
	
	/**
	 * 
	 */
	@Override
	public Map<String, String> getBatchStatus() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch: getBatches()) 
				if (batch.isRunnnig())
					map.put(batch.getName(), batch.getStatus());
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
		return map;
	}
	

	/**
	 * 
	 */
	@Override
	public Map<String, String> getRunningRequestsStr() {
	
		Map<String, String> map = new HashMap<String, String>();
		
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch: getBatches()) { 

				if (batch.isRunnnig()) {
					
					for (ServiceRequest ser: batch.getRequests()) {
						
						if (ser.startExecutingTimestamp()>0 && ser.endExecutingTimestamp()<1) {
							
							StringBuilder str = new StringBuilder();
							
							str.append("Class -> " + ser.getClass().getName());
							str.append("\n Name -> " + (ser.getName()!=null?ser.getName():""));
							str.append("\n Description -> " + (ser.getDescription()!=null?ser.getDescription():"null"));
							
							
							if (ser.getProgress()>0.0)
								str.append("\n Progress -> " + (String.valueOf(ser.getProgress())));
							
							if (ser.startExecutingTimestamp() > 0)
								str.append("\n Duration -> " + (String.format( "%9.2f", (System.currentTimeMillis()-ser.startExecutingTimestamp())/1000.0)+" secs"));
						
							if (ser.getObjectID()!=null&& ser.getObjectID().length()>0) 
								str.append("\n ObjectID -> " + (ser.getObjectID()!=null?ser.getObjectID():"null"));
							
							if (ser.getParameters()!=null)
								str.append("\n Parameters -> " + ser.getParameters().toString() );
							
							map.put(ser.getName()!=null?ser.getName():"null", str.toString());
							
						}
					}
					
				}
			}
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
		return map;
	}

	
	
	@Override
	public Map<String, String> getConfigurableParameters() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			this.batches_lock.readLock().lock();
			
			map.put("scheduler.maxinram", String.valueOf(MAX_ALLOWED_IN_RAM) );
			map.put("scheduler.restms",   String.valueOf(fixedRestMs) );
			map.put("scheduler.batch-size",   String.valueOf(BATCH_SIZE) );
			
			map.putAll(getQueue().getConfigurableParameters());
			
			 
			
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
		return map;
	}

	@Override
	public void addRequestToken(Serializable key) {
		request_token.put(key, key);
	}
	
	@Override
	public void removeRequestToken(Serializable key) {
		request_token.remove(key);
	}
	
	public boolean isSleeping() {
		return this.sleeping;
	}
	
	@Override
	public boolean containsRequestToken(Serializable key) {
		if (logger_tokens.isDebugEnabled() && request_token.containsKey(key)) 
				logger_tokens.debug("contains({}), {}", key, request_token.containsKey(key)?"yes":"no");
		return request_token.containsKey(key);
	}
	
	
	
	
	@Override
	public void processCronJobById(Serializable id) {
		
		Iterator<AbstractCronJobRequest> it = getCronJobsList().iterator();
		while (it.hasNext()) {
			
			AbstractCronJobRequest job = it.next();
			if (job.getId().toString().equals(id.toString())) {
				
				try {
					lock(CRON_JOBS_LOCK);
					logger.debug("Processig: " + job.toString());
					processRequest(job);
					dispatchAll();
					return;
				}
				finally {
					unlock(CRON_JOBS_LOCK);
				}	
			}
		}
		logger.debug("Not found: " + id.toString());

	}
	
	
	/** 
	 * Only Thread Scheduler
	 * @throws SchedulerException
	 */
	private void processQueue() throws SchedulerException {
		
		ServiceRequest request = null;
		this.maxRoundCapacity.set(false);

		int round = 0;
		
		try {
			this.last_queue_processing = Instant.now();

			while (  !getQueue().isEmpty() && 
					 !resetReceived() && 
					 !isFull(round)) {

				request = getQueue().dequeue();
				processRequest(request);
				round++;
			}
				
			if (isFull(round))
				this.maxRoundCapacity.set(true);

			if (!resetReceived())
				dispatchAll();
		} 
		catch (SchedulerException e)  {
			logger.debug(String.valueOf(round));
			logger.error(e);
			blogger.error(e);
			throw(e); 
		} 
		catch (Exception e)  {
			blogger.error(e);
			logger.error(e);
			logger.debug(String.valueOf(round));
			throw new SchedulerException(e);
		} 
	}
	
	private boolean isFull(int round_size) {
		return round_size >= getMaxInRound();
	}
	
	private boolean isTooManyRequests() {
		return this.getTotalInBatches()>MAX_ALLOWED_IN_RAM;
	}
	
	private boolean isWork() {
		if (this.incoming.get()>0)
			return true;
		if (this.maxRoundCapacity.get())
			return true;
		if (resetReceived())
			return true;
		return false;
	}
	
	

	
	/** 
	 * <p>Cron Jobs CronExpressions are calculated using the ZonedTime of the java VM</p>
	 * 
	 */
	private void processCronJobs() {
	
		ZonedDateTime now = ZonedDateTime.now();

        AbstractCronJobRequest job = getCronJobsList().getFirst();
        final ZonedDateTime time = job.getTime();
		
		logger.debug("First Cron Request >> " + job.toString());
		
		
		if (job!=null && now.isAfter(time)) {
			try {
				lock(CRON_JOBS_LOCK);
				job = getCronJobsList().getFirst();
				if (now.isAfter(job.getTime())) {
					if (job.isEnabled()) {
						job = getCronJobsList().pollFirst();
						logger.debug("-------------------------------------------------------");
						logger.debug("process Request -> " + job.toString());
						logger.debug("-------------------------------------------------------");
						processRequest(job);
						dispatchAll();
					} else {
						logger.debug("Request -> " + job.toString() +" | is not enabled");
					}
				}
				if(job.getExecuteOldTriggers()) {
					ServiceLocator.getService(CronSchedulerService.class).updateCronLastExecution((Long) job.getId(), job.getTime().toOffsetDateTime());
				}
			}
			finally {
				unlock(CRON_JOBS_LOCK);
			}
		}
	}

	private void processRequest(ServiceRequest request) {
		logger.debug("Into Batch: " + request.getName() + " priority: " + String.valueOf(request.getPriority()));
		Batch batch = getBatch(request);
		if (batch.isReady()) {
			logger.debug("Dispatching [ready] " + batch.getName() + ". "  + String.valueOf(batch.getRequests().size()) + " items");
			dispatch(batch);
		}
	}
	
	private void dispatchAll() {
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch : getBatches()) {
				if (!batch.isRunnnig() && !batch.isDispatched() && batch.getSize()>0) {
					logger.debug("Dispatching [not_ready] " + batch.getName() + ". "  + String.valueOf(batch.getRequests().size()) + " items. Priority " + batch.getPriority());
					dispatch(batch);
				}
			}
		} 
		finally {
			this.batches_lock.readLock().unlock();
		}
	}

	/** 
	 * only Scheduler Thread
	 * ST
	 * 
	 * @param request
	 * @return
	 */
	private Batch getBatch(ServiceRequest request) {
		Batch batchofrequest = null;
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch : getBatches()) {
				if (batch.getPriority() == request.getPriority()) {
					if ((!batch.isRunnnig()) && (!((KbeeBatch)batch).isDispatched()) && (((batch.getCost() + request.getCost()) <= batch.getCapacity())) ) {
						batch.add(request);
						batchofrequest = batch;
						break;
					}
				}
			}
		}
		finally {
			this.batches_lock.readLock().unlock();
		}
		if (batchofrequest==null) {
			batchofrequest = new KbeeBatch("Batch "+ String.valueOf(++batch_counter), request.getPriority(), getBatchSize()) {
				// If all the Requests were successful. This is done from the Batch Thread
				@Override
				public void onAfterRun() {
					try {
						getMeterThroughput(this.getPriority()).mark(this.getSize());
						for (ServiceRequest request : getRequests()) {
							getQueue().remove(request);
						}	
					}
					catch (SchedulerException e) {
						logger.error(e);
						throw new KbeeRuntimeException(e);
					}
				}
				// Si falla un request se retorna a la cola con error para ser procesado nuevamente
				@Override
				public void onError(ServiceRequest request, Throwable error) {
					try {
						request.setError(error);
						getQueue().enqueue(request);
					}
					catch (SchedulerException e1) {
						logger.error(e1);
						throw new KbeeRuntimeException(e1);
					}
				}
				// Se liberan todos los recursos del thread del batch
				@Override
				public void doFinally() {
					getQueue().dispose();
				}
			};
			if (request.getPriority()==0) {
				((KbeeBatch)batchofrequest).setCapacity(1);
			}
			batchofrequest.add(request);
			try {
				this.batches_lock.writeLock().lock();
				this.batches.add(batchofrequest);
			} 
			finally {
				this.batches_lock.writeLock().unlock();
			}
		}
		return batchofrequest;
	}

	private void dispatch(Batch batch)  {
		getDispatchers().get(batch.getPriority()).dispatch(batch);
	}
	
	private List<Batch> getBatches() {
		return this.batches;
	}

	private void setSleeping(boolean value) {
		sleeping = value;
	}
	
	private void setStatus(SchedulerStatus status) {
		this.status=status;
	}
	
	private SchedulerStatus getInternalStatus() {
		return this.status;
	}
	
	private boolean resetReceived() {
		return this.status == SchedulerStatus.RESETTING;
	}
	
	/**
	 *    when a Resetcommand has been received from the outside world.
	 * 
	 * @throws SchedulerException
	 */
	private void clearBuffers(boolean force) throws SchedulerException {
		
		logger.warn("ClearBuffers. All Batches " +(force?"not running ": "") + "will be discarded.");
		
		List<Batch> list = new ArrayList<Batch>();
		
		try {
			this.batches_lock.readLock().lock();
			for (Batch batch: this.batches) {
				if (!batch.isRunnnig() || force) { 
					logger.debug("Removing - > " + batch.getName());
					list.add(batch);
				}
			}
		} 
		finally {
   			this.batches_lock.readLock().unlock();
		}

		try {
			batches_lock.writeLock().lock();
			for (Batch batch: list) {
				try {
				if (force && batch.isRunnnig())
					batch.stop();
				} catch (Exception e) {
					logger.error(e);
				}
				this.batches.remove(batch);
			}
		} 
		finally {
			this.batches_lock.writeLock().unlock();
		}
	}

	private void rest() {
		ZonedDateTime next = getCronJobsList().getFirst()!=null ? getCronJobsList().getFirst().getTime() : null;
		ZonedDateTime now = ZonedDateTime.now();

		long value;

		if (next!=null && now.plusSeconds(CONSERVATIVE_SIESTA_SECS).isAfter(next)) {
			logger.debug("Siesta is longer than next cronjob. sleep until next cronjob -> " + ( getCronJobsList().getFirst()!=null?( getCronJobsList().getFirst().getName() + " " + next.toString()): "null"));
			value = Duration.between(now, next).toMillis();
		}
		else
			value = CONSERVATIVE_SIESTA_SECS * 1000;
		rest(value);
	}
	
	private void rest(long value) {
		try {
			if (value>0) {
				synchronized (this) {
					logger.debug("Set to sleep: " + String.valueOf(value) + " ms");
					setSleeping(true);
					this.wait(value);
					setSleeping(false);
					logger.debug("Again awake.");
				}
			}
		} 
		catch (InterruptedException e) {
			logger.debug("Again awake -> " + e.getClass().getName());	
			logger.debug(e.getMessage());
			setSleeping(false);
		}
		catch (Throwable e) {
			logger.error(e);
			blogger.error(e);
			setSleeping(false);
		}
	}
	
	private void criticalRest() {
		try {
			synchronized (this) {
				logger.debug("Set to sleep: " + String.valueOf(30000) + " ms");
				setSleeping(true);
				this.wait(30000);
				setSleeping(false);
				logger.debug("Again awake.");
			}
		} 
		catch (InterruptedException e) {
			logger.debug("Again awake -> " + e.getClass().getName());	
			setSleeping(false);
		}
	}
	
	private String getCriticalStatus() {
		StringBuilder message = new StringBuilder();
		message.append("shceduler critical error. ");
		try {
			message.append(String.valueOf(getQueueSize()) + " elements in scheduler queue. "); 
		}
		catch (Throwable e) {
			message.append(e.getMessage());
		}
		return message.toString();
	}
	
	private void addNotifySynchronization() {
		
		Synchronization synchronization = transactions.get(Thread.currentThread());
		org.hibernate.Transaction transaction = getSessionFactory().getCurrentSession().getTransaction();

		if (synchronization == null || !((ThreadSynchronization)synchronization).inTransaction(transaction)) {
			synchronization = new ThreadSynchronization(transaction) {
				@Override
				public void beforeCompletion() {
				}
				@Override
				public void afterCompletion(int status) {
					synchronized (KbeeSchedulerService.this) {
						KbeeSchedulerService.this.incoming.addAndGet(1);
						KbeeSchedulerService.this.notifyAll();
						transactions.remove(Thread.currentThread());
					}
				}
			};
			transaction.registerSynchronization(synchronization);
			transactions.put(Thread.currentThread(), synchronization);
		}
	}
	
	private void lock(String value) {
		ServiceLocator.getService(ValueLockerService.class).lock(value);
	}
	
	private void unlock(String value) {
		ServiceLocator.getService(ValueLockerService.class).unlock(value);
	}
	
	private Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction();
	}
	
	private void rollback(Transaction transaction) {
		try {
			if (transaction!=null && transaction.isActive() && !transaction.isCompleted()) {
				transaction.rollback();
			}
		}
		catch (Throwable e) {
			blogger.error(e);
			logger.error(e, "error in tx rollback");
		}
	}
	
	

	@Override
	public boolean listen(Event event) {
		return event instanceof EvictCronJobsListEvent;
	}

	@Override
	public void onEvent(Event event) {
	if (event instanceof EvictCronJobsListEvent) {
			try {
				lock(CRON_JOBS_LOCK);
				cronJobs.reset();
			} 
			catch (Exception e) {
				logger.error(e);
			}
			finally {
				unlock(CRON_JOBS_LOCK);
			}
		}
		
	}
	
	public double getOneMinuteInputRateHp() {
		return getMeterInputHP().getOneMinuteRate();
	} 
	
	public double getFifteenMinuteInputRateHp() {
		return getMeterInputHP().getFifteenMinuteRate();
	}
	
	public double getFiveMinuteInputRateHp() {
		return getMeterInputHP().getFiveMinuteRate();
	}
	
	public double getOneMinuteInputRateLp() {
		return getMeterInputLP().getOneMinuteRate();
	}
	
	public double getFifteenMinuteInputRateLp()	{
		return getMeterInputLP().getFifteenMinuteRate();
	}
	
	public double getFiveMinuteInputRateLp() {
		return getMeterInputLP().getFiveMinuteRate();
	}
	
	// ThroughPut: counts the rate at which Requests are completed
	//
  	public double getOneMinuteThroughPutHP() {
  		return getMeterThroughputHP().getOneMinuteRate();
  	}
  	
	public double getFiveMinuteThroughPutHP() {
		return getMeterThroughputHP().getFiveMinuteRate();
	}
	
	public double getFifteenMinuteThroughPutHP() {
		return getMeterThroughputHP().getFifteenMinuteRate();
	}

	public double getOneMinuteThroughPutLP() {
		return getMeterThroughputLP().getOneMinuteRate();
	}
	
	public double getFiveMinuteThroughPutLP() {
		return getMeterThroughputLP().getFiveMinuteRate();
	}	
	
	public double getFifteenMinuteThroughPutLP() {
		return getMeterThroughputLP().getFifteenMinuteRate();
	}

	public double  getMeanHPIn() {
		return getMeterInputHP().getMeanRate();
	}
	
	public double  getMeanLPIn() {
		return getMeterInputLP().getMeanRate();
	}
											
	public double  getMeanHPOut() {
		return getMeterThroughputHP().getMeanRate();
	}
	
	public double  getMeanLPOut() {
		return getMeterThroughputLP().getMeanRate();
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	} 
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}


	private Meter getMeterThroughput(int priority) {
		return ((priority==HIGH_PRIORITY) ? getMeterThroughputHP() : getMeterThroughputLP());
	}

	private Meter getMeterInput(int priority) {
		return ((priority==HIGH_PRIORITY) ? getMeterInputHP() : getMeterInputLP());
	}
	
	private Meter getMeterInputHP() {
		if (metric_requests_hp==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_requests_hp = mt.getMetrics().meter(MetricRegistry.name(KbeeSchedulerService.class, "requests", "hp"));
		}
		return metric_requests_hp;
	}
	
	private Meter getMeterInputLP() {
		if (metric_requests_lp==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_requests_lp = mt.getMetrics().meter(MetricRegistry.name(KbeeSchedulerService.class, "requests", "lp"));
		}
		return metric_requests_lp;
	}

	private Meter getMeterThroughputHP() {
		if (metric_throughput_hp==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_throughput_hp = mt.getMetrics().meter(MetricRegistry.name(KbeeSchedulerService.class, "throughput", "hp"));
		}
		return metric_throughput_hp;
	}

	private Meter getMeterThroughputLP() {
		if (metric_throughput_lp==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_throughput_lp = mt.getMetrics().meter(MetricRegistry.name(KbeeSchedulerService.class, "throughput", "lp"));
		}
		return metric_throughput_lp;
	}

	private boolean isRunning() {
		return this.status == SchedulerStatus.RUNNING;
	}
	
	private boolean isResseting() {
		return this.status == SchedulerStatus.RESETTING;
	}

	private int getMaxInRound() {
		return maxInRound;
	}
	
	private int getBatchSize() {
		return BATCH_SIZE;
	}
	
	 //new Reflections(
	//		   new ConfigurationBuilder()
	//		     .addUrls(ClasspathHelper.forPackage("org.reflections"))   // add urls for package prefix
	//		     .addScanners(Scanners.values())                           // use all standard scanners
	//String name="com.novamens.scheduler"; // AbstractServiceRequest.class.getName();// ServiceRequest.class.getName();
	//Reflections reflections = new Reflections("com.novamens.scheduler", org.reflections.scanners.Scanners.values());
	

	// Set<Class<?>> modules =  reflections.get(org.reflections.scanners.Scanners.SubTypes.of(AbstractServiceRequest.class).asClass());

	private List<Class<? extends ServiceRequest>> request_classes;
	
	public List<Class<? extends ServiceRequest>> getAllServiceRequestClasses() {
		
		if (request_classes!=null) 
			 return request_classes;
		 
		synchronized (this) {
		try {	
			
			request_classes = new ArrayList<Class<? extends ServiceRequest>>();
			
			
			{
			 Reflections reflections = new Reflections(
					  new ConfigurationBuilder()
					    .forPackage("com.novamens")
					    .setScanners(org.reflections.scanners.Scanners.values())
					    .filterInputsBy(new FilterBuilder().includePackage("com.novamens")));
			

			 
			Set<Class<?>> modules =  reflections.get( org.reflections.scanners.Scanners.SubTypes.of(ServiceRequest.class).asClass());
			
			logger.debug("reflections.get( org.reflections.scanners.Scanners.SubTypes.of(ServiceRequest.class).asClass()) -> " + modules.size());

			modules.forEach(item -> 
			{
				
				// ---
				// if (!Modifier.isAbstract( item.getModifiers()))
				//	     request_classes.add(item);
				// ---

				request_classes.add( (Class<? extends ServiceRequest>) item);
				logger.debug(item.getName());
			}
			);
			}
			

			{
			 Reflections reflections = new Reflections(
					  new ConfigurationBuilder()
					    .forPackage("kbee")
					    .setScanners(org.reflections.scanners.Scanners.values())
					    .filterInputsBy(new FilterBuilder().includePackage("kbee")));
			

			 
			Set<Class<?>> modules =  reflections.get( org.reflections.scanners.Scanners.SubTypes.of(ServiceRequest.class).asClass());
			
			logger.debug("reflections.get( org.reflections.scanners.Scanners.SubTypes.of(ServiceRequest.class).asClass()) -> " + modules.size());

			modules.forEach(item -> 
			{
				//if (!Modifier.isAbstract( item.getModifiers()))
				//	request_classes.add(item);

				request_classes.add( (Class<? extends ServiceRequest>) item);
				logger.debug(item.getName());
			}
			);
			}
			
			
			
			request_classes.sort(new Comparator<Class<? extends ServiceRequest>>() {
				@Override
				public int compare(Class<? extends ServiceRequest> o1, Class<? extends ServiceRequest> o2) {
					return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
				}
				
			});
			} catch (Exception e) {
				logger.error(e);
			}
		}
		

		return request_classes;

	}


	@Override
	public void stopAllThreads() {
		
		logger.debug("-------------------------------------------------------------------------");
		logger.debug("first we will try to reset the main loop");
		setStatus(SchedulerStatus.RESETTING);
		rest(5000);
		
		logger.debug("current status -> " + getInternalStatus());
		
		for (Entry<Integer, Dispatcher> e: getDispatchers().entrySet()) {
			logger.debug("killing Dispatcher ->  " + e.getValue().getInfo());
			e.getValue().shutDownNow();
		}
		
		logger.debug("killing Scheduler Thread -> " + this.getThread().getName());
		this.getThread().interrupt();
		
		setStatus(SchedulerStatus.STOPPED);
		logger.debug("done");
		logger.debug("-------------------------------------------------------------------------");
		
		//
		// now we should create all threads again and put the scheduler to work
		//
		
	}

}

