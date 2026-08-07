package com.novamens.scheduler;


import java.io.Serializable;
import java.util.Map;

public interface SchedulerQueue <T extends Serializable> {

	/**
	 * Returns the id in the Database (Long)
	 */
	public Serializable enqueue(T object) throws SchedulerException;
	
	
	public T		dequeue() 				throws SchedulerException;
	public void 	remove(T object) 		throws SchedulerException;
	public boolean 	isEmpty() 				throws SchedulerException;
	public int 		getSize() 				throws SchedulerException;
	public int 		getErrorSize()			throws SchedulerException;
	public void 	resetQueue() 			throws SchedulerException;
	public void 	cleanPhantomRequests()  throws SchedulerException;

	public void setBufferSize(int size);
	
	public void 	dispose();
	
	/**
	 * 
	 * @return
	 * <p>Examples:
	 * ok
	 * 
	 * 20 requests older than 5 minutes.
	 * </p>
	 * @throws SchedulerException
	 */
	public String getQueueStatus() throws SchedulerException;
	void restartQueue() throws SchedulerException;
	public Map<String, String> getConfigurableParameters();
	
	
}
