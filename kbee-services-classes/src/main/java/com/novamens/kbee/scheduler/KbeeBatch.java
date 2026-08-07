package com.novamens.kbee.scheduler;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.beans.BeansService;
import com.novamens.scheduler.Batch;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.service.ServiceLocator;

/**
 * Implementation of the interface {@link Batch}
 * 
 * <p>It uses a Database Transaction</p>
 * 
 */
public class KbeeBatch implements Batch {

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	static private kbee.util.logging.Logger blogger = kbee.util.logging.Logger.getLogger(KbeeBatch.class.getName());
	
	private int priority;
	private int cost;
	private int capacity;
	private int initial_capacity;
	
	private boolean running 	= false;
	private boolean terminated 	= false; 
	private boolean dispatched 	= false;
	private boolean haserrors 	= false;
	private boolean allexecuted	= false;
	
	private int executed = 0;
	private String name;
	
	private List<ServiceRequest> requests = Collections.synchronizedList(new ArrayList<ServiceRequest>());
  
	private ServiceRequest request = null;
	
	private long ts_created;
	private long ts_start_running;
	private long ts_dispatched;
	private long ts_terminated;
	
	private long total_round_trip = 0;
		
	private AtomicBoolean is_stop = new AtomicBoolean(false);
									
	
	public KbeeBatch(int priority, int capacity) {
		this("Batch", priority, capacity);
	}
	
	public KbeeBatch(String name, int priority, int capacity) {
		setInitialCapacity(capacity);
		setCapacity(capacity);
		setPriority(priority);
		setTimestampCreated(System.currentTimeMillis());
		this.name = name;
	}

	public boolean isStopped() {
		return is_stop.get();
	}
		
	@Override
	public void stop() {
		is_stop.set(true);
		if (this.request != null)
			this.request.stop();
	}
	
	/**
	 * <p> {@link Request} at this point have a Database Id<p>
	 */
	@Override								
	public synchronized List<String> getServiceRequestStatus() {
		List<String>list = new ArrayList<String>();
		synchronized (requests) {
			try {
				for (ServiceRequest request: requests) {
					try {
						list.add("id. " + request.getId().toString() + " |  "+ request.getDescription());
					} catch (Exception e) {
						logger.error(e);
						blogger.error(e);
						list.add(e.getClass().getName() + " | " + e.getMessage());
					}
				}
			} finally {
				if (logger.isDebugEnabled()) {
					logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName()+": ");
					list.forEach(item -> logger.debug(item));
				}
			}
		}
		return list;
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized String getStatus() {
		StringBuilder str = new StringBuilder();

				if (this.running)	   str.append(" Running");
		else 	if (this.terminated)   str.append(" Terminated " + (isStopped()?" Stopped" : ""));
		else    if (this.dispatched)   str.append(" Dispatched ");
		else 	if (this.haserrors)    str.append(" Idle with Errors ");
		else						   str.append(" Idle "); // terminated with error 
				
		str.append(" | Total Requests: " + String.valueOf(this.getRequests().size()));
		str.append(" | Executed: " + String.valueOf(this.executed));
		
		if (this.isDispatched() && !this.isRunnnig())
			str.append(" | WARNING: Dispatched but not yet Running.");
			
		if (this.isRunnnig())
			str.append(" | Running for: " + String.valueOf((System.currentTimeMillis()- this.getTimestampStartRunning())/1000) + " secs");
		
		if (this.isTerminated())
			str.append(" | Terminated - Run length: " + String.valueOf(  (this.getTimestampTerminated() - this.getTimestampStartRunning())/1000) + " secs");
		
		return str.toString(); 
	}
	

	@Override
	public void run() {
		
		assert(!this.running);

		this.request = null;
		this.running = true;
		this.is_stop.set(false);
		this.executed = 0;
		this.allexecuted = false;
		this.haserrors = false;
		this.total_round_trip = 0;
		this.ts_start_running=System.currentTimeMillis();
		
		logger.debug("Starting execution - Batch: " +  getName() + " #Requests: " + String.valueOf(getRequests().size()));
		
		try {

			if (getRequests()!=null) {
 				openSession();
 				beginTransaction();
				Iterator<ServiceRequest> requests = getRequests().iterator();
				
				int execounter = 0;
				
				while (requests.hasNext() && !isStopped()) {
					request = requests.next();
					logger.debug("Start Request: " + request.getId().toString() + " | " + request.toString() );
					request.setStartExecutingTimestamp(System.currentTimeMillis());
					request.execute();
					request.setEndExecutingTimestamp(System.currentTimeMillis());
					logger.debug("End Request:    " + request.getId().toString() + " (" + String.valueOf(request.endExecutingTimestamp()-request.startExecutingTimestamp()) +" ms) ");
		
					// round trip measures the time it takes from
					// that enters the queue until it starts executing
					total_round_trip += (request.startExecutingTimestamp()-request.inQueueTimestamp());
					execounter++;
				}

				if (isStopped()) 
					throw new InterruptedException("Stop received");
				
				onAfterRun();
				allexecuted = true;
				commit();
				this.requests.clear();
				this.executed+=execounter;
			}
 			
 			setCapacity(getIntialCapacity()); // if all requests are completed, the capacity of the batch must be the initial 
 			setTimestampTerminated(System.currentTimeMillis());
 			this.terminated = true;
 			long end=System.currentTimeMillis();			
			logger.debug("End Batch "+ getName() +". Duration: " + String.valueOf( (double)(end-this.ts_start_running)/1000.0) + " secs.  Total: " + String.valueOf(executed) + " Requests executed.");

		}
		catch (Throwable e) {
			try {
				
				logger.error(e, (request!=null? request.getName():"null"));
				blogger.error(e);
				
				// when there is an unexpected problem, remove the problematic request
				// and the batch should be rerun (minus the problematic request)
				if (request!=null)
					request.setEndExecutingTimestamp(System.currentTimeMillis());
				
				// The capacity is reduced to the current cost - the cost of the broken request. 
				// The idea is that this Batch will re-execute without
				// getting any other request (in order to make starvation not possible).
				if ((request!=null) && (getSize()>1))
					setCapacity(getCost()-request.getCost());
				
				StringBuilder str = new StringBuilder();
				str.append(e.getClass().getName()+ " | ");
				if (e.getCause()!=null)
					str.append(e.getCause().toString());
				else
					str.append(e.getClass().getName());
				
				if (request!=null)
					str.append(". | req: " + request.getClass().getName() + " + " + request.getDescription());
				
				logger.error("[ KbeeBatch ] | " + str.toString());
				
				rollback();
				
				setTimestampTerminated(System.currentTimeMillis());
				long end=System.currentTimeMillis();
				
				logger.debug("End Batch "+ getName() +"[ERROR]. Duration: " + String.valueOf( (double)(end- this.ts_start_running)/1000.0) + " secs.  Total: " + String.valueOf(executed));

				this.haserrors   = true;

				beginTransaction();
				
				// si no puedo determinar cual falla saco el ultimo o saco todos?
				if (allexecuted) {
//					for (ServiceRequest request : requests) {
//						onError(request, e);
//					}	
//					this.terminated  = true;
					if (request!=null) {
						requests.remove(request);
						onError(request, e);
					}
					this.terminated  = false;
				}
				else {
					if (request!=null) {
						requests.remove(request);
						onError(request, e);
					}
					this.terminated  = false;
				}
				
				commit();

			}
			catch (Throwable e1) {
				logger.error(e1);
				blogger.error(e1);
			}
		}
		finally {
			doFinally();
			closeSession();
			logger.debug("closeSession() -> " + this.getName());
			this.request 	= null;
 			this.running 	= false;
 			this.dispatched = false;
 			if (getRequests()!=null) {
 				this.cost=0;
 				for (ServiceRequest r: getRequests())
 					this.cost+=r.getCost();
 			}
		}
	};
	
	
	@Override
	public String toString() {
		return getName();
	}
	
	public void add(ServiceRequest request) {
		request.setInBatchTimestamp(System.currentTimeMillis());
		this.cost += request.getCost();
		this.requests.add(request);
	}
	
	public List<ServiceRequest> getRequests() {
		return requests;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int value) {
		this.priority = value;
	}
	
	public int getCost() {
		return cost;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int value) {
		this.capacity = value;
	}
	
	public boolean isRunnnig() {
		return running;
	}
	
	public boolean isTerminated() {
		return terminated;
	}

	public int executed() {
		return executed;
	}
	
	public boolean isReady() {
		return getCost()>=getCapacity() && !isTerminated();
	}
	
	public boolean isDispatched() {
		return dispatched;
	}
	
	public void setDispatched(boolean value) {
		this.dispatched = value;
	}
	
	public long getTimestampCreated() {
		return ts_created;
	}
	
	public long getTimestampDispatched() {
		return ts_dispatched;
	}
	
	public long getTimestampTerminated() 	{
		return ts_terminated;
	}
	
	public void setTimestampCreated(long ts) {
		ts_created=ts;
	}
	
	public void setTimestampDispatched(long ts)	{
		ts_dispatched=ts;
	}
	
	public void setTimestampTerminated(long ts)	{
		ts_terminated=ts;
	}
	
	public String getName() { 
		return name;
	}
					
	public long getTimestampStartRunning() 	{
		return this.ts_start_running;
	}
	
	public int getSize() {
		return requests.size();	
	}

	public long getTotalRoundTrip() {
		return  total_round_trip; 
	}	

	public long getMeanRoundTrip() {
		if (executed==0)
			return -1;
		return  total_round_trip / executed; 
	}	
	
	protected void onError(ServiceRequest request, Throwable e) {}
	protected void onAfterRun() 								{}
	protected void doFinally() 									{}
	
	/**
	 * Database Transaction
	 */
	private void beginTransaction()  {
		getTransaction().begin();
		
		SessionFactory sessionFactory = getSessionFactory();
		SessionHolder holder = (SessionHolder)TransactionSynchronizationManager.getResource(sessionFactory);
		holder.setTransaction(getTransaction());
		if (!TransactionSynchronizationManager.isSynchronizationActive())
		TransactionSynchronizationManager.initSynchronization();
	}
	
	private void commit()  {
		try {
			getTransaction().commit();
			for (TransactionSynchronization ts : TransactionSynchronizationManager.getSynchronizations()) {
				ts.afterCompletion(0);
			}
		}
		catch (Exception e) {
			if (TransactionSynchronizationManager.isSynchronizationActive())
			for (TransactionSynchronization ts : TransactionSynchronizationManager.getSynchronizations()) {
				ts.afterCompletion(1);
			}
			throw e;
		}
		finally {
//			if (TransactionSynchronizationManager.isSynchronizationActive())
//				TransactionSynchronizationManager.clearSynchronization();
		}
	}
	
	private void rollback()  {
		try {
			getTransaction().rollback();
			if (TransactionSynchronizationManager.isSynchronizationActive())
			for (TransactionSynchronization ts : TransactionSynchronizationManager.getSynchronizations()) {
				ts.afterCompletion(1);
			}
		}
		finally {
//			if (TransactionSynchronizationManager.isSynchronizationActive())
//				TransactionSynchronizationManager.clearSynchronization();
		}
	}
	
	private Transaction getTransaction() {
		return getSessionFactory().getCurrentSession().getTransaction();
	}
	
	private SessionFactory getSessionFactory() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sessionFactory = (SessionFactory)beans.getBean("sessionFactory");
		return sessionFactory;
	}
	
	/**
	 * Hibernate Session 
	 * @return
	 */
	private SessionFactory openSession() {
		SessionFactory sessionFactory = getSessionFactory();
		if(!TransactionSynchronizationManager.hasResource(sessionFactory)) {
			Session session = sessionFactory.openSession();
			session.setHibernateFlushMode(FlushMode.COMMIT);
			SessionHolder holder = new SessionHolder(session);
			TransactionSynchronizationManager.bindResource(sessionFactory, holder);
		}	
		return sessionFactory;
	}
	
	/**
	 * 
	 */
	private void closeSession() {
		SessionFactory sessionFactory = getSessionFactory();
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
	}
	
	private int getIntialCapacity() {
		return this.initial_capacity;
	}
	
	
	private void setInitialCapacity(int c) {
		initial_capacity=c;
	}




}
