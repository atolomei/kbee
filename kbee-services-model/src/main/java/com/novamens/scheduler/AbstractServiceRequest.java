package com.novamens.scheduler;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Map;

import com.novamens.security.Identifiable;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;


/**
 *  <p>Requests that are executed by the {@link SchedulerService} Worker Thread does not need to open a Hibernate Session or a Database Transaction, they are managed by the Scheduler Batch  (@link Batch}).</p>
 *  <p>If the Request launches a new Thread -for example via a  Async  {@link Command}-, in this case the Command will have to manage its' <b>Hibernate Session</b> and <b>Database Transaction</b></p>
 *  
 *  @see {@link Batch} 
 *  @see {@link AbstractCronJobRequest} for Cron based ServiceRequest
 *  
 *  <b>IMPORTANT</b>: All subclasses must be {@link Serializable} because the <@link SchedulerService} stores them on the Database
 */
public abstract class AbstractServiceRequest implements ServiceRequest, Identifiable {
	
	private static final long serialVersionUID = 1L;
	
	
	static public boolean isCrobJobRequest() {
		return false;
	}
	
	static String _server_id;
	static String _hostname;
	
		
	/** this value is set up by the {@link KbeeSchedulerQueue} (ie. the Scheduler Queue) from a Database Sequencer */
	private Long id;
	
	private int priority = SchedulerService.LOW_PRIORITY;
	private int cost = SchedulerService.STANDARD_PROCESSING_COST;
	
	private int errors = 0;
	private String errorMessage;
	private String name;
	
	
	private long inqueue;
	private long inbatch;
	
	private String applicationserverid;
	private String hostname;
	
	
	private long startexecuting  = 0;
	private long endexecuting = 0;

	private String description;
	private String objectid;
	
	private OffsetDateTime execute_after;
	
	private Map<String, String> parameters;
	
	
	public AbstractServiceRequest() {
		
	}
	
	public AbstractServiceRequest(Map<String, String> map) {
		setParameters(map);
	}

	@Override
	public String getServerHost() {
		if (this.hostname==null)
			this.hostname= ServiceLocator.getService(ApplicationServerService.class).getServerHost();
		return hostname; 
	}
	
	@Override
	public String getApplicationServerId() {
		if (applicationserverid==null)
			this.applicationserverid =  ServiceLocator.getService(ApplicationServerService.class).getApplicationServerId();
		return applicationserverid; 
	}
	
	public void setExecuteAfter(OffsetDateTime d) {
		execute_after= d;
	}
	
	public OffsetDateTime getExecuteAfter() {
		return this.execute_after;
	}
	
	public Serializable getId()	{
		return id;
	}
	
	public void setId(Long value) {
		this.id = Long.valueOf(value);
	}
	
	public void setPriority(int value) {
		this.priority = (value<0?1:value);
	}
	
	public int getPriority() {
		return (priority<0?1:priority);
	}
	
	public void setCost(int value) {
		this.cost = (value<1?1:value);
	}
	
	public int getCost() {
		return (cost<1?1:cost);
	}
	
	public void setError(Throwable error) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		errorMessage = sw.toString(); 
		errors++;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public int getErrors() {
		return errors;
	}

	@Override
	public String getName() {
		if (this.name!=null)
			return this.name;
		return getClass().getSimpleName();		
	}
	
	@Override
	public void setName(String name) {
		this.name=name;
	}
	
	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public void setParameters(Map<String, String> map) {
		parameters = map;
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	/**
	 * <p>toString is used to display programmer's level info of the Object</p>
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getName() + " | ");
		return str.toString();
	}
	
	public long inQueueTimestamp() 						{return inqueue;}
	public long inBatchTimestamp() 						{return inbatch;}
	public long startExecutingTimestamp() 				{return startexecuting;}
	public long endExecutingTimestamp() 				{return endexecuting;}
	
	public void setInQueueTimestamp(long t) 			{inqueue=t;}
	public void setInBatchTimestamp(long t) 			{inbatch=t;} 
	
	public void setStartExecutingTimestamp(long t) 		{startexecuting=t;}
	public void setEndExecutingTimestamp(long t)		{endexecuting=t;}
	
	public boolean isCronJob() 								{return false;}
	
	@Override			
	public String getObjectID() 						{return this.objectid;}
	
	@Override
	public void setObjectID(String des) 				{this.objectid=des;}

	@Override
	public String getDescription() 						{return description;}
	
	@Override
	public void setDescription(String des) 				{description=des;}
	
	@Override
	public double getProgress() {return 0;}

	@Override
	public void stop() {
	}
	
	protected String getWorkDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath();
	}

	protected String getDataExportDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "dataexport";
	}
}
