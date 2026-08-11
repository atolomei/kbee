package com.novamens.kbee.content.command;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

import kbee.util.PropertiesFactory;

public class ListMissingFilesCommand extends AbstractCommand  implements Runnable  {

	static Logger logger = LogManager.getLogger(ListMissingFilesCommand.class.getName());


	static private final SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss z");
	static public final String FIELD_SEPARATOR  = " | ";	
	static public final String NA 				= " N/A ";
	
	
	private int total_scanned 			= 0; 
	private int files_missing			= 0;
	private int files_ok				= 0;
	private int file_errors 			= 0; 
	private long total_files_to_process = 0; 

	private boolean aborted 	= false;

	private Thread thread;
	private boolean running;
	
	private SessionFactory sf;
	
	public ListMissingFilesCommand() {
		setName("List Missing Files");
		setPriority(SchedulerService.LOW_PRIORITY);
	}

	
	/**
	 * 
	 * 
	 * 
	 */

	public void executeTask() {
		
		setDateStarted(OffsetDateTime.now());
		
		setProgress(0);

		BufferedWriter file_log = null;
		
		try { 
				// open Hibernate Session
				//
				sf = com.novamens.hibernate.session.Session.open();

				// Authenticate
				// 
				ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
				
				this.setStatusInfo("Calculating Size");
				this.total_files_to_process = ((Long)sf.getCurrentSession().createQuery("select count(*) from KBFileImpl K ").uniqueResult()).longValue();
																																
				Query query = sf.getCurrentSession().createQuery("from KBFileImpl ");

				//List<?> results = query.list();

				Iterator<?> iterator = query.iterate();

				boolean done = false;

				this.setStatusInfo("Starting processing " + String.valueOf(this.total_files_to_process) + " files");
				
				
				// abrir el log
				//
				//
				
				String file_name = "missingfiles.log";
				
				File fg = new File(getWorkDir() + File.separator + file_name);
				
				if (fg.exists()) {
					try {
						KbeeFileUtils.forceDelete(fg);
					} catch (IOException e) {
							logger.error(e.getStackTrace());
					}
				}
				
				file_log = new BufferedWriter(new FileWriter(fg));
				
				file_log.write("Missing Files Report\n");
				file_log.write("\n");
				
				file_log.write("Started: ");
				file_log.write(dateformat.format(new Date()) + "\n\n");
				

				while (iterator.hasNext() && !isStopped() && !aborted && !done) {

					if (this.file_errors>200) 
						this.aborted=true;
					
					try {
						Object object1 = iterator.next();
											
						KBFile kfile = ((KBFile) object1); 
						
						File file = kfile.getFile();
						
						if (file==null) {
							this.files_missing++;
							file_log.write(kfile.getBucketName() + " " + kfile.getObjectName() + "\n");
							
						}
						else {
							this.files_ok++;
						}
					} catch (Exception e) {
						this.file_errors++;
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					}
					finally {
						this.total_scanned++;
						this.setStatusInfo("Scanned " + String.valueOf(this.total_scanned) + " / " +  String.valueOf(this.total_files_to_process));
					}
				}
				
				
				if (!this.aborted && !isStopped()) {
					setProgress(100);
					setResult("OK");
					setState(CommandState.COMPLETED);
					setDateTerminated(OffsetDateTime.now());
					
					setResultComments(
							"Total Files scanned: " + String.valueOf(this.total_scanned) +  
							". Files missing: " + String.valueOf(this.files_missing) +  
							". Files ok: " + String.valueOf(this.files_ok) + 
							". Errors: " + String.valueOf(this.file_errors));
					
					file_log.write("\n\n----\nTotal Files scanned: " + String.valueOf(this.total_scanned) +  
							"\nFiles missing: " + String.valueOf(this.files_missing) +  
							"\nFiles ok: " + String.valueOf(this.files_ok) + 
							"\nErrors: " + String.valueOf(this.file_errors) + "\n");
				}
				else {
					
					if (this.aborted) {
						setResult("Error");
						setState(CommandState.ERROR);
						setDateTerminated(OffsetDateTime.now());
						setResultComments("Reached 200 Errors, aborted !");
						file_log.write("\n\n\"Reached 200 Errors, aborted !" + "\n");
						
					} 
					else {
						setResult("Canceled by user");
						setState(CommandState.CANCELED);
						setDateTerminated(OffsetDateTime.now());
						setResultComments("Canceled");
						file_log.write("\n\n\"Cancelled by User " + "\n");
					}
				}
				
				logger.debug("Ending Command execution " + getName());
				
				
		} catch (Throwable e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			setResult(e.getClass().getSimpleName());
			setResultDetails(e.getMessage());
			// setResultComments("Added CRC to " +String.valueOf(this.files_crc_applied)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + ". Errors: " + String.valueOf(file_errors));
			setState(CommandState.ERROR);
			setDateTerminated(OffsetDateTime.now());
			
		}
	
		finally {
			
			if ( file_log!=null) {
				try {
					file_log.write("\n----\n");
					file_log.write("End: ");
					file_log.write(dateformat.format(new Date()) + "\n\n");
					file_log.close();
					
				} catch (IOException e) {
					logger.error(e);
				}
			}
			
			com.novamens.hibernate.session.Session.close();	
			setStatusInfo("DB Session closed.");
		}	
	}

	/** 
	 * 
	 */
	
	@Override
	public void execute() {
		this.thread = new Thread(this);
    	this.thread.setDaemon(false);
    	this.thread.setName(getName());
    	this.thread.setPriority(Thread.NORM_PRIORITY);
    	this.thread.start();
	}

	/**
	 * 
	 *  
	 */

	@Override
	public void run() {
		setState(CommandState.RUNNING);
		setRunning(true);
		executeTask();
	}
	
	/**
	 * 
	 */
	protected void setRunning(boolean value) {
    	this.running = value;
	}
	
	/**
	 * 
	 */
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	/**
	 * 
	 */
	private Object reload(Object object) {
		return getContentDao().reload(object);
	}
	

}
