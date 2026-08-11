package com.novamens.kbee.content.email;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.email.EmailData;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailSendServiceRequest;

			
public class TestEmailSendCommand extends AsyncCommand {

	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger(TestEmailSendCommand.class.getName());

	int total_emails_to_send = 200;
	int delay = 50;
	
	int ONLY_EMAIL;
	int EMAIL_AND_TEST;
	int mode = ONLY_EMAIL;

	private DateTimeFormatter df = DateTimeFormatter.ofPattern ( "HH:mm:ss", Locale.ENGLISH);


	private SessionFactory sf;

	/**
	 * 
	 */
	public TestEmailSendCommand() {
	}

	/**
	 * 
	 */
	public TestEmailSendCommand(int intValue) {
		this.total_emails_to_send = intValue;
	}

	/**
	 * 
	 */
	@Override
	protected void executeAsync() {
	
		boolean error = false;
		try {
			
			setState(CommandState.RUNNING);
			setProgress(0);
			
			logger.info("Starting Test Email ");
			logger.info("Wait 5 seconds for the calling thread and request to end.");
			
			try {
				Thread.sleep(5*1000);
			} catch (InterruptedException e) {
				logger.error(e);
			}
			

			this.sf = com.novamens.hibernate.session.Session.open();
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName().trim());
			
			
			for (int n=0; n<total_emails_to_send; n++) {
				schedule(getNewRequest(n));
				setProgress(100 * n / total_emails_to_send);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						logger.error(e);
					}
			}
			
			logger.info("done.");
			
		} catch (Exception e) {

			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getMessage());
			error=true;
			setStatusInfo(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			super.setResultComments(e.getMessage());
			setState(CommandState.ERROR);
			
		} finally {
				
			//if (this.sf!=null)
			//	this.sf.close();
			
			com.novamens.hibernate.session.Session.close();
			
			
			if (!error) {
				setProgress(100);
				setState(CommandState.COMPLETED);
			}
			
			setStatusInfo("Terminated.");
		}
	}

	
	/**
	 * @param n
	 * @return
	 */
	private EmailSendServiceRequest getNewRequest(int n) {
		OffsetDateTime da = OffsetDateTime.now();
		String from = "Sender " + String.valueOf(n);
		String to = "Receiver "  + String.valueOf(n);
		String subject = df.format(da);
		String msg = "Message ";
		EmailData data = new EmailData(from, to, subject, msg, "test");
		EmailSendServiceRequest re = new EmailSendServiceRequest(data, getDomain()); 
		return re;
	}
	

	/**
	 * 
	 */
	
	public Domain getDomain() {
		return getDomainKbee();
	}
	
	/**
	 * 
	 */
	private Domain getDomainKbee() {
		return getContentDao().findDomainByName ("kbee");
	}

	/**
	 * 
	 */
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/**
	 * 
	 */
	private void schedule(ServiceRequest task) {
		Transaction transaction = null;
		boolean is_ok= false;
		try {
			transaction = sf.getCurrentSession().beginTransaction();
			ServiceLocator.getService(SchedulerService.class).enqueue(task);
			is_ok = true;
		}
		catch (SchedulerException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			is_ok = false;
		}
		 finally {
				if (transaction!=null) {
					if (is_ok)
						transaction.commit();
					else
						transaction.rollback();
				}
			}
	}
	

}
