package com.novamens.kbee.content.command;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;


import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.TransactionService;

public class FixFileSizeCommand extends AsyncCommand {
			
	static Logger logger = LogManager.getLogger(FixFileSizeCommand.class.getName());
	
	private SessionFactory sf;
	private long total_files_to_process = 0; // 

	private int total_scanned 			= 0; 
	private int files_touched 			= 0;
	private int files_ok	 			= 0;
	private int files_not_found			= 0;
	private int file_errors 			= 0; 

	private boolean aborted 	= false;
	
	
	public FixFileSizeCommand() {
		setName("Check and fix KBFile sizes");
		setPriority(SchedulerService.LOW_PRIORITY);
	}
	
	@Override
	protected void executeAsync() {
		
		try {

			total_files_to_process 	= 0;
			total_scanned 			= 0; 
			files_touched 			= 0;
			files_ok	 			= 0;
			files_not_found			= 0;
			file_errors 			= 0; 
			
			setDateStarted(OffsetDateTime.now());
			setProgress(0);
			
			// open Hibernate Session
			sf = com.novamens.hibernate.session.Session.open();

			// Authenticate
			// 
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");  
			
			this.setStatusInfo("Calculating size");
			this.total_files_to_process = ((Long)sf.getCurrentSession().createQuery("select count(*) from KBFileImpl").uniqueResult()).longValue();
																															
			Query<?> query = sf.getCurrentSession().createQuery("from KBFileImpl K order by K.id desc");

			List<?> srclist = query.list();
			List<Object> list = new ArrayList<Object>();

			this.setStatusInfo("Starting processing " + String.valueOf(this.total_files_to_process) + " files");
			logger.info("Starting processing " + String.valueOf(this.total_files_to_process) + " files");
			

			for (Object object1: srclist) {

				if (isStopped() || aborted)
					break;
				
				list.add(object1);
				this.total_scanned++;
				
				if (list.size()==20) {
					processList(list);
					list.clear();
				}
																																			
				this.setStatusInfo("Scanned " + String.valueOf(this.total_scanned) + " / " +  String.valueOf(this.total_files_to_process) + ". Fixed: " +  String.valueOf(this.files_touched));
				logger.info("Scanned " + String.valueOf(this.total_scanned) + " / " +  String.valueOf(this.total_files_to_process) + ". Fixed: " +  String.valueOf(this.files_touched));
				
				if (this.file_errors>100) 
					aborted=true;
			}
			
			
			
			
			if (list.size()>0  && !isStopped() && !aborted) {
				processList(list);
				list.clear();
			}
			
			if (!aborted && !isStopped()) {
				setProgress(100);
				setResult("OK");
				setState(CommandState.COMPLETED);
				setDateTerminated(OffsetDateTime.now());
				
				setResultComments(
						  "Total      : " + String.valueOf(this.total_files_to_process) + " " +
						"| Processed  : " + String.valueOf(this.total_scanned) + " " +
						"| Fixed      : " + String.valueOf(this.files_touched) + " " +
						"| Were OK    : " + String.valueOf(this.files_ok)  + " " +
						"| Not Found  : " + String.valueOf(this.files_not_found)  + " " +
						"| Errors     : " + String.valueOf(file_errors));
			}
			else {
				
				if (aborted) {
					setResult("Error");
					setState(CommandState.ERROR);
					setDateTerminated(OffsetDateTime.now());												
					setResultComments("Fixed " +String.valueOf(this.files_touched)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + " OK Files: "+ String.valueOf(this.files_ok) + ". Errors: " + String.valueOf(file_errors));

				} 
				else {
					setResult("Canceled by user");
					setState(CommandState.CANCELED);
					setDateTerminated(OffsetDateTime.now());								
					setResultComments("Fixed " +String.valueOf(this.files_touched)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + " OK Files: "+ String.valueOf(this.files_ok) + ". Errors: " + String.valueOf(file_errors));
				}
			}
			
			logger.debug("Ending Command execution " + getName());
		
		} catch (Throwable e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				setResult(e.getClass().getSimpleName());
				setResultDetails(e.getMessage());						
				setResultComments("Fixed " +String.valueOf(this.files_touched)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + " OK Files: "+ String.valueOf(this.files_ok) + ". Errors: " + String.valueOf(file_errors));
				setState(CommandState.ERROR);
				setDateTerminated(OffsetDateTime.now());
			
		} finally {
			
			com.novamens.hibernate.session.Session.close();	
			setStatusInfo("DB Session closed.");	
		}
	}
	
	
	protected com.novamens.transaction.Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Object reload(Object object) {
		return getContentDao().reload(object);
	}
	
	
	/**
	 * 
	 * @param object
	 * @return
	 */
	private boolean isElegible(Object object) {
		
		if (object==null)
			return false;
				
		try {
			if (    (object instanceof KBFile)	&&
				  (((KBFile) object).isBinaryFile())) 
				return true;
		} catch (IOException e) {
			logger.error(e);
			return false;
		}

		return false;
	}
	

	private void processList(List<Object> list) {

		com.novamens.transaction.Transaction transaction = null;

		logger.info("ProcessList start");
		
		try {
			transaction = beginTransaction();
			for (Object obj: list) {
					Object object = reload(obj);
					if (isElegible(object)) {
							if (((KBFile) object).isBinaryFile()) {
											java.io.File fi=((KBFile) object).getFile();
											if ( fi!=null) { 
												long size = fi.length();
												if (((KBFile) object).getSize()!=size) {
													
													if (object instanceof KBFileImpl)
														((KBFileImpl) object).setSize(size);
													
													getContentDao().save(((KBFile) object));
													this.files_touched++;
													logger.info(((KBFile) object).getTitle() + " fixed ");
												}
												else  {
													this.files_ok++;
													logger.info(((KBFile) object).getTitle() + " ok ");
												}
											}
											else {
												logger.info(((KBFile) object).getTitle() + " not found ");
												files_not_found++;
											}
							}
							else {
								logger.info(((KBFile) object).getTitle() + " file not found ");
								files_not_found++;
							}
						
							logger.info("[ " + String.valueOf(getProgress())+"% ]");
					}
					else {
						logger.info(((KBFile) object).getTitle() + " not elegible or not found ");
						files_not_found++;
					}

					setProgress(this.total_files_to_process>0? ((int) 100 * this.total_scanned / (int) this.total_files_to_process):100);
			}
			
			logger.info("ProcessList: commit");
			transaction.commit();
		
		} catch (Throwable e) {
				logger.error("error", e);
				transaction.rollback();
				this.file_errors+=list.size();
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " |  " + e.getMessage());
				throw new ContentMgmtException(e);
			}
	}
	
	



}
