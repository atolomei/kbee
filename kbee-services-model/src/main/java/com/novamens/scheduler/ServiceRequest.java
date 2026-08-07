package com.novamens.scheduler;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;


/**
 *<p>Base interface of services to be sent to the Scheduler
 * The key method is {@code execute} which is executed by the {@link Batch} when the {@link Dispatcher} of the {@link SchedulerSevice} launches it. 
 *</p>
 *
 *
 *<p>ServiceRequest are serialized and saved into a BLOB field in the Database by the {@link SchedulerService}, therefore they must be Serializable</p>
 *
 */
public interface ServiceRequest extends Serializable {

	
 
	static public boolean isCrobJobRequest() {
		return false;
	}
	
	/** Unique id provided by a Database Sequencer  */
	public Serializable getId();
	
	public void execute();
	
	/** 0.00 - 1.00. Not all Requests implement it */
	public double getProgress();  
	
	public int getPriority();
	public int getCost();
	
	public default boolean isExecuting() {return (startExecutingTimestamp()>0 && endExecutingTimestamp()<1);}
	
	public void setError(Throwable error);
	public String getErrorMessage();
	public int getErrors();
	
	/** static information about the request. It should not include the dynamic info -progress, when started, ...- */
	public String toString();
	
	public long inQueueTimestamp();
	public long inBatchTimestamp(); 
	public long startExecutingTimestamp();
	public long endExecutingTimestamp();
	
	public void setInQueueTimestamp(long t);
	public void setInBatchTimestamp(long t); 
	public void setStartExecutingTimestamp(long t);
	public void setEndExecutingTimestamp(long t);
	
	public boolean isCronJob();
	
	public String getName();
	public void setName(String name);

	public String getDescription();
	public void setDescription(String des);

	public void stop();

	public void setParameters(Map<String, String> map);
	public Map<String, String> getParameters();


	public String getObjectID();
	public void setObjectID(String des);

	public void setExecuteAfter(OffsetDateTime d);
	public OffsetDateTime getExecuteAfter();

	public String getServerHost();
	//public void setServerHost(String s);
	
	public String getApplicationServerId();
	//public void  setApplicationServerId(String s);
	
	
}
