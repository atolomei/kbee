package com.novamens.kbee.content.command;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
			
public class MinioFileServerImportCommand extends AbstractCommand {
				
	static Logger logger = LogManager.getLogger(MinioFileServerImportCommand.class.getName());

	private Domain domain 				= null;

	private int total_scanned 			= 0; 
	private int files_crc_applied 		= 0;  
	private int file_errors 			= 0; 
	private int max_files_to_apply  	= 0; 
	private long total_files_to_process = 0; 
	
	private boolean aborted = false;

	private SessionFactory sf;
	
	/**
	 * 
	 * This Test Command
	 * takes N files from the Domain
	 * and imports them into the Minio File Server 
	 * 
	 * 
	 */
	public MinioFileServerImportCommand() {
		setName("MinioFileServerImport");
		setPriority(SchedulerService.HIGH_PRIORITY);
		this.max_files_to_apply = 0;
	}
	

	
	public MinioFileServerImportCommand(int limit) {
		setName("MinioFileServerImport");
		setPriority(SchedulerService.LOW_PRIORITY);
		this.max_files_to_apply = limit;
	}
			

	public MinioFileServerImportCommand(Domain domain, int limit) {
		setName("MinioFileServerImport");
		setDomain(domain);
		this.max_files_to_apply = limit;
		setPriority(SchedulerService.LOW_PRIORITY);
	}

	
	/** 
	 * 
	 */
	@Override
	public void execute() {
		executeTask();
	}
	
	
	/** 
	 * 
	 */
	@SuppressWarnings({"rawtypes" })
	public void executeTask() {
		
		setDateStarted(OffsetDateTime.now());

		setProgress(0);

		try { 
				
			// THis is required for Scheduler 
			// open Hibernate Session
			//
			sf = com.novamens.hibernate.session.Session.open();

			// Authenticate
			// 
			if (getDomain()!=null) 
				ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName().trim());
			else
				ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

			this.total_scanned=0;
			
			this.setStatusInfo("Calculating size");
			
			this.total_files_to_process = ((Long)sf.getCurrentSession().createQuery("select count(*) from KBFileImpl K where  is null" + (getDomain()!=null?  " and K.domain.id=" + getDomain().getId().toString(): "")).uniqueResult()).longValue();
																																
			Query query = sf.getCurrentSession().createQuery("from KBFileImpl K where K.crc32str is null" + (getDomain()!=null ?  " and K.domain.id=" + getDomain().getId().toString(): ""));
				
			//List<?> results = query.list();

			@SuppressWarnings("deprecation")
			Iterator<?> iterator = query.iterate();

			List<Object> list = new ArrayList<Object>();
				
			boolean done = false;

			this.setStatusInfo("Starting processing " + String.valueOf(this.total_files_to_process) + " files");

			int errno = 0;
			
			while (iterator.hasNext() && !isStopped() && !aborted && !done) {
				
					try {
						Object object1 = iterator.next();
						list.add(object1);
					
						this.total_scanned++;
						
						if (list.size()==10) {
							processList(list);
							list.clear();
						}
						
						this.setStatusInfo("Scanned " + String.valueOf(this.total_scanned) + " / " +  String.valueOf(this.total_files_to_process) + ". CRC: " +  String.valueOf(this.files_crc_applied)  + " Limit: " + String.valueOf(this.max_files_to_apply));
						
						if (this.max_files_to_apply>0 && this.max_files_to_apply<=(this.files_crc_applied+this.file_errors) )
							done=true;
						
						if (this.file_errors>100) 
							aborted=true;
						
					} catch (Exception e) {
						logger.error(e.getClass().getName(), e);
						errno++;
						if (errno>10)
								throw new RuntimeException(e);
					}
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
					
					setResultComments("Added CRC 32 to " +String.valueOf(this.files_crc_applied)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + ". Errors: " + String.valueOf(file_errors));
				}
				else {
					
					if (aborted) {
						setResult("Error");
						setState(CommandState.ERROR);
						setDateTerminated(OffsetDateTime.now());
						setResultComments("Reached 100 Errors, aborted ! Added CRC 32 to " +String.valueOf(this.files_crc_applied)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + ". Errors: " + String.valueOf(file_errors));
					} 
					else {
						setResult("Canceled by user");
						setState(CommandState.CANCELED);
						setDateTerminated(OffsetDateTime.now());
						setResultComments("Canceled. Added CRC 32 to " +String.valueOf(this.files_crc_applied)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + ". Errors: " + String.valueOf(file_errors));
					}
				}
				
				logger.debug("Ending Command execution " + getName());
			
			} catch (Throwable e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					setResult(e.getClass().getSimpleName());
					setResultDetails(e.getMessage());
					setResultComments("Added CRC to " +String.valueOf(this.files_crc_applied)+" Objects. Total Files to process: " + String.valueOf(this.total_files_to_process) + ". Errors: " + String.valueOf(file_errors));
					setState(CommandState.ERROR);
					setDateTerminated(OffsetDateTime.now());
			}
			
			finally {
				com.novamens.hibernate.session.Session.close();	
				setStatusInfo("DB Session closed.");
			}
	}

	
	/** 
	 * 
	 */
	private boolean isElegible(Object object) {
		
		if (object==null)
			return false;
				
		try {
			if (	(object instanceof KBFile           )	&&
				  (((KBFile) object).getCRC32()==null	)	&& 
				  (((KBFile) object).isBinaryFile()   )) 
				return true;
		} catch (IOException e) {
			logger.error(e.getClass().getName(), e);
			return false;
		}

		return false;
	}
	
	
	
	/** 
	 * 
	 */
	private void processList(List<Object> list) {

		Transaction transaction = sf.getCurrentSession().beginTransaction();

		try {
		
			for (Object obj: list) {
				try {
					Object object = reload(obj);
						if (isElegible(object)) {
									try {
										if (((KBFile) object).getFile()!=null) {
		
											//  long crc32 = org.apache.commons.io.FileUtils.checksumCRC32(((KBFile) object).getFile());
											//  ((KBFile) object).setCRC32(Long.toHexString(crc32));
											
												String sha= KbeeFileUtils.calculateSHA256String(((KBFile) object).getFile());
												getContentDao().save(((KBFile) object));
												this.files_crc_applied++;
											
											((KBFile) object).setSHA256(sha);
										}
														
									} catch (ContentMgmtException e1) {

										logger.error(e1.getStackTrace());
										this.file_errors++;
										
									} catch (IOException e) {
										
										logger.error(e.getStackTrace());
										this.file_errors++;

									
									} catch (Exception e) {
										logger.error(e);
										this.file_errors++;

									}

						}
						
						logger.info("[ " + String.valueOf(getProgress())+"% ]");
	
						setProgress((int) this.total_files_to_process>0?(int) 100 * this.total_scanned/ (int) this.total_files_to_process:100);
						
				} catch (RuntimeException e) {
					logger.error(e.getClass().getName(), e);
					setProgress((int) this.total_files_to_process>0?(int) 100* this.total_scanned/ (int) this.total_files_to_process:100);
					this.total_scanned++;
				}
			}
		}
		finally {
				try {
					
					transaction.commit();

				} catch (HibernateException  e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					this.file_errors+=list.size();
					try {
						transaction.rollback();
					} catch (HibernateException  e1) {
						logger.error(e.getClass().getName(), e);
					}
				}
		}
	}

	/** 
	 * 
	 */
	public void setDomain(Domain domain) {
		this.domain = domain;
		setDomainId(domain.getId());
	}
	
	/** 
	 * 
	 */
	public Domain getDomain() {
		if (domain == null) {
			if (getDomainId() == null) {
				domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else {
				try {
					long did = (Long.valueOf(getDomainId().toString())).longValue();
					domain = getContentDao().findDomainById(did);
				} catch (Throwable e) {
					logger.error(e.getClass().getName(), e);
				}
			}
		}
		return domain;
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
