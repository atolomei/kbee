 package com.novamens.content.command;

import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.content.resource.KBFile;
import com.novamens.dom.DomainObject;


/**
 * 
 * <p>Important: Command are not (as of version 6.5) persistent. 
 * they do not survive application restart </p> 
 *
 */
public interface Command extends com.novamens.security.Identifiable, DomainObject {

	public final int SCOPE_DOMAIN_ADMIN  = 1;
	public final int SCOPE_DOMAIN_ROOT   = 2;
	public final int SCOPE_KBEE_ROOT   	 = 3;
	public final int SCOPE_ALL			 = 4;

	public Serializable getId();
	
	public String getName();
	
	public String getTitle();
	
	public void setDescription(String des);
	
	public String getDescription();
	
	public String getHelp();
	
	public void execute();
	
	public int getScope();
	
	public Object getParameter(String name);
	
	public void setParameter(String name, Object value);
	
	public CommandState getState();
	
	public void setState(CommandState state);
	
	/** Executing ------------------------------------------------------------
	 */
	
	/* 0.00 to 100.00 */
	public double getProgress();
	
	/* milliseconds*/
	public long getDuration();
	
	/* seconds */
	public double estimatedSecsToEnd();
	
	public String getStatusInfo();

	/**  After termination --------------------------------------------------
	 */
	public boolean isTerminated();
	public String getResult();
	public String getResultComment();
 
	/** Info -----------------------------------------------------------------
	 */
	public OffsetDateTime getDateCreated();
	public OffsetDateTime getDateStarted();
	public OffsetDateTime getDateTerminated();
	
	public String getResultDetails();
	public void setResultDetails(String rsd);

	public KBFile getResultFile();
	public File getLogFile();

	public void stop();
	public boolean isStopped();
	
	public void pause();
	
	public boolean isPaused();
	public void resume();
	
	public int getPriority(); 
	public void setPriority(int p);
	
	public Serializable getDomainId();
	public void setDomainId(Serializable did);
	
	public List<String> getMetadataAsList();

	public Serializable getUserId();
	public void setUserId(Serializable did);

	public void setParameters(Map<String, Object> map);
	public Map<String, Object> getParameters();
	public  void end();

	public long getTotalItemsProcessed();
	public long getTotalItems();

	public String getKey();
	

	/** number of threads that execute this command. By default=1*/
	public int getThreads();
	
	/**
	 * <p>if the Scheduler restarts the Request and executes the Command for a second time it will not execute again.</p>
	 */
	public boolean isExactlyOneSemantics();
	public void setExactlyOneSemantics(boolean exactlyOneSemantics);
	
	
	/**
	 * <p>If the command must manage <b>Database Transaction</b>
	 *  <br />
	 *  <b>Scheduler Thread</b>  
	 *  Commands that are executed by Scheduler Threads include
	 *  Hibernate Session and Database Transaction
	 *  <br />
	 *  <b>From UI</b>
	 *  Commands that are executed by from the UI do not include
	 *  Hibernate Session and Database Transaction
	 *  but they can use Services that
	 *  are Spring beans and have the @Transactional Annontation
	 *  <br />
	 *  <b>Own Thread</b>
	 *  Commands that run on their own thread must
	 *  manage their Hibernate Session and Database Transaction
	 * </p>
	 */
	public void setRequiresExplicitTrx(boolean b);
	public boolean isRequiresExplicitTrx();

	public void addCallback(CommandLifecycleCallback commandLifecycleCallback);

	public String getConcurrentUniqueKey();

	String getStringParameter(String name, String defaultValue);
	List<CommandParameter> getParametersDefinition();
}
