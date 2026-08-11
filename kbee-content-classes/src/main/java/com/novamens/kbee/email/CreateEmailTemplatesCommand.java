package com.novamens.kbee.email;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.logging.usage.LogApiUsageServiceRequest;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class CreateEmailTemplatesCommand extends AbstractCommand {

	static Logger logger = LogManager.getLogger(CreateEmailTemplatesCommand .class.getName());

	// private DateTimeFormatter df = DateTimeFormatter.ofPattern ( "yyyy-MM-dd", 	Locale.ENGLISH);
	
	private boolean is_trx = false;
	
	 public CreateEmailTemplatesCommand() {
		 setExactlyOneSemantics(true);
	 }

	 
	/**
	 * 
	 * This must be used from a Spring Bean
	 * For Scheduler based log: {@link LogApiUsageServiceRequest}
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 	insert into scheduler 
				( ID, 
				  REQUEST, 
				  TIME, 
				  PRIORITY, 
				  ERROR_COUNT, 
				  ERROR_MESSAGE, 
				  DESCRIPTION, 
				  TITLE, 
				  OBJECTID, 
				  EXECUTE_AFTER, 
				  COMMAND_CLASS_NAME, 
				  COMMAND_PARAMETERS, 
				  HOSTNAME, 
				  APPSERVERID) 
				
				VALUES
				 
				(
				(select nextval('domainid_sequence')), 
				null, 
				now(), 
				1, 
				0,
				null, 
				'check and add email templates', 
				'check and add email templates', 
				null, 
				null, 
				'com.novamens.kbee.email.CreateEmailTemplatesCommand', 
				'', 
				null, 
				'universal');
				
				
				
				
	 */
	@Override
	public void execute() {
			   
		
		setDateStarted(OffsetDateTime.now());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			logger.error(e1);
		}
		
		
		setProgress(0);
		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
		
		try {
		
					

					int total = getContentDao().getDomains().size();
					int index = 0;
				
					for (Domain dom: getContentDao().getDomains()) {
						if (dom.getName()!=null && !dom.getName().equals("kbee")) {
								processDomain(dom);
						}
						index++;
						setProgress(index/total);
					}
					
 					setProgress(100);
 					setResult("OK");
 					setState(CommandState.COMPLETED);
 					
 					
	 	} catch (Exception e) {
	 		
	 		
	 				setState(CommandState.ERROR);
	 				setResult(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());

					//  
					// Database transactions that fail must propagate the exception
					// for the Scheduler to rollback and mark the request
					// 
					throw(e);
		}
		finally {
			setDateTerminated(OffsetDateTime.now());
			logger.debug( "done" );
		}
 	}

	private void processDomain(Domain dom) {
		logger.debug( "Processing domain -> " + dom.getDisplayName() );
			ServiceLocator.getService(EmailService.class).setUpTemplates(dom);
	}


	/**
	 * 
	 * CommandBean -> RequestCommand -> Scheduler TRX
	 * RequestCronJob -> Scheduler TRX
	 * 
	 * From UI -> must include trx here
	 * 
	 * @param b
	 */
	public void setTrx(boolean b) {
		this.is_trx=b;
	}
	
	
	public boolean isTrx() {
		return this.is_trx;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	

}
