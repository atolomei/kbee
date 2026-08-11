package com.novamens.kbee.content.command;

import java.io.File;
import java.io.IOException;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;


import com.novamens.content.resource.KBFile;

import com.novamens.content.service.UserImagesService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * This command executes in its own thread 
 */

public class AssignPhotoUsersCommand extends AbstractCommand implements Runnable  {

 	private static Logger logger = LogManager.getLogger(AssignPhotoUsersCommand.class.getName());

	@SuppressWarnings("unused")
	static private Logger trx_logger = LogManager.getLogger("TxLogger");
	

	private int errors = 0;
	private int converted = 0;
	private int processed= 0;
	private int total =0;
	
	private Thread thread;
	private boolean running;


	private SessionFactory sf;
	
	public AssignPhotoUsersCommand (Domain domain) {
		setName("Assign User Photo " + String.valueOf(getId()));
		setDomain(domain);
		setPriority(SchedulerService.HIGH_PRIORITY);
	}

	
	
	@Override
	public void run() {
		setState(CommandState.RUNNING);
		executeTask();
	}
	
	
	
	public boolean isRunning() {
	    	return this.running;
	}
	
	
	protected void setRunning(boolean value) {
    	this.running = value;
	}
	
	
	@Override
	public void execute() {

		this.thread = new Thread(this);
    	this.thread.setDaemon(false);
    	this.thread.setName(getName());
    	this.thread.setPriority(Thread.NORM_PRIORITY);
    	this.thread.start();
	}

	
	
	
private void executeTask() {
		
		logger.debug("Starting Command execution " + getName());

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		
		try {
				// open Hibernate Session
				//
				this.sf = com.novamens.hibernate.session.Session.open();
				
				errors 		= 0;
				converted 	= 0;
				processed	= 0;
				total 		= 0;
		
				UserImagesService service = ServiceLocator.getService(UserImagesService.class);
				
				List<UserProfile> list;

				list = getContentDao().getUserProfiles();
					
				total = list.size();
				
				
				ServiceLocator.getService(SecurityService.class).authenticate("root@kbee" );
				//domain
				
				logger.info("Starting to process " + String.valueOf(total));
				
				for(UserProfile up: list) {
					
					if (this.isStopped()) {
						break;
					}
				
					try {
					
						Person person = up.getPerson();
						
						if (person!=null) {
							
							boolean has_real_photo=false;
							
							if (person.getPhoto()!=null) {
								
								KBFile photo = person.getPhoto();
								
								try {
									File pf=photo.getFile();
									if (pf!=null && pf.exists())
										has_real_photo=true;
									else
										logger.debug(photo.getTitle() + " file not found");
									
								} catch (IOException e) {
									logger.debug(photo.getTitle() + " file not found");
								}
							}
							
							
							if (!has_real_photo && (up.getUser()!=null) && (up.getUser().getUserName()!=null)) {
								
								
								KBFile file = service.getDefaultImage(up.getUser().getUserName());
								
								boolean is_ok = false;
								
								if (file!=null) {
									Transaction transaction = null;
									try {
										
											transaction = sf.getCurrentSession().beginTransaction();
											person.setPhoto(file);
											getContentDao().save(person);
											
											// Por el momento no funciona
											// 
											converted++;											
											logger.debug("User: " + up.getUser().getDisplayName() + "(username: " + up.getUser().getId().toString() +  "  (" + String.valueOf(getProgress()+"%) "));
											is_ok = true;
										
									} catch (ContentMgmtException e) {
										
										is_ok = false;
										errors++;
										logger.error(e.getClass().getName(), e);
										
										if (errors > 30) {
											this.stop();
											break;
										}
									} finally {
										if (transaction!=null) {
											if (is_ok)
												transaction.commit();
											else
												transaction.rollback();
										}
									}
								}
								else {
									logger.debug("No image for user " + person.getDisplayName());
									if (errors++ > 5) {
										this.stop();
									}
								}
							}
						}
					}  
					finally {
						processed++;
						setProgress((int)(100*processed/total));
						//logger.info("progress: " + getProgress());
					}
				}
					
				setDateTerminated(OffsetDateTime.now());
		
				StringBuilder str = new StringBuilder();
				str.append("Total " + String.valueOf(total));
				str.append(". Scanned " + String.valueOf(processed));
				str.append(". Photo added " + String.valueOf(converted));
				str.append(". Errors " + String.valueOf(errors));
				
				setResultComments(str.toString());
				
				if (!isStopped()) {
					setProgress(100);
					setResult("OK");
					setState(CommandState.COMPLETED);
				}
				else {
					setResult("Cancelled by User");
					setState(CommandState.CANCELED);
				}
		
				logger.debug("Ending Command execution " + getName());
		
		} finally {
			
				com.novamens.hibernate.session.Session.close();	
				setStatusInfo("DB Session closed.");
		}
}
	

	public String getStatement() {
		return (String)getParameter("statement");
	}




	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


}
