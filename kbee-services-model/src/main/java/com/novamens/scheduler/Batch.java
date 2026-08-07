package com.novamens.scheduler;

import java.util.List;


/**
 * 
 * <p>A Batch is a Thread that executes a set of {@link ServiceRequest} inside 1 DB Transaction 
 *
 */
public interface Batch extends Runnable {

	public void run();
	public void add(ServiceRequest request);
	public List<ServiceRequest> getRequests();
	public int getPriority();
	
	/** total cost in (units of work) of the Service Requests in contains */
	public int getCost();
	
	public int getCapacity();
	
	/** number of Requests it contains */
	public int getSize();
	
	/** ie: if the weight of the requests is larger than the Batch capacity */ 
	public boolean isReady();
	
	public boolean isDispatched();
	public boolean isRunnnig();
	public boolean isTerminated();
	
	public String getName();
	
	public   long getTimestampCreated();
	public	 long getTimestampDispatched();
	public 	 long getTimestampTerminated();
	public 	 long getTimestampStartRunning();
	
	public   void setTimestampCreated(long ts);
	public	 void setTimestampDispatched(long ts);
	public 	 void setTimestampTerminated(long ts);

	public void setDispatched(boolean value);
	
	
	/**
		Status of each of the Service Requests
		They should be dead before being "Dispatched"
		some running during Dispatch all running on termination.
		Inside the Dispatcher they can be Waiting if no threads are available.	  
	*/
	public List<String> getServiceRequestStatus();
	public String getStatus();
	public void stop();
	
}
