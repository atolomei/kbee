package com.novamens.kbee.content.notes;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;

public class CreateWelcomeNoteCommand extends AsyncCommand {
			
 

	private static Logger logger = LogManager.getLogger(CreateWelcomeNoteCommand.class.getName());
	
	
	 	private SessionFactory sf;
	 
	 
		private int errors = 0;
		private int processed= 0;
		private int total =0;

		
		 public CreateWelcomeNoteCommand() {
			 setName("Add Welcome Note " + String.valueOf(getId()));
			 setPriority(SchedulerService.HIGH_PRIORITY);
		 }

		 
		
	@Override
	protected void executeAsync() {

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		try {
				// open Hibernate Session
				//
				this.sf = com.novamens.hibernate.session.Session.open();
		
				errors 		= 0;
				processed	= 0;
				total 		= 0;
		
				SystemParameter title_p = getContentDao().findSystemParameterByKey("welcome-note.title");
				String title = title_p!=null?title_p.getValue():"What is My Notepad ?";
				
				SystemParameter text_p = getContentDao().findSystemParameterByKey("welcome-note.text");
				String text = text_p!=null?text_p.getValue():"<p>The Notepad is a panel easily accesible from the toolbar where you can create and manage simple notes.</p><p>Notes can include&nbsp;<a href=\"http://kbee.io\">links</a> and formats like <strong>bold</strong> or <em>italic</em>.</p><p>&nbsp;</p><p>Notes are private, no one else can read or edit them.</p>";

				
				
				List<Domain> list = getContentDao().getAllDomains();
				
				for (Domain domain: list) 
					total+=getContentDao().getTotalUsers(domain);
				
				if (total>0) {
					for (Domain domain: list) {

								List<UserProfile> ups = getContentDao().findUserProfileByDomain(domain);
								ServiceLocator.getService(SecurityService.class).authenticate("root@" + domain.getName().trim());
			
								for (UserProfile u: ups) {
									if (this.isStopped()) 
											break;
									
									processed++;
									
									boolean is_ok = false;
									
										if (u.getUser()!=null)  {
											Transaction transaction = null;
											try {
													transaction = sf.getCurrentSession().beginTransaction();
													KbeeUserNote note = new KbeeUserNote(u.getUser());
													note.setTitle(title);
													if (text!=null)
														note.setText(text);
													getContentDao().save(note);
													is_ok = true;
											}
											catch (Exception e) {
													is_ok = false;
													errors++;
													logger.error(e.getStackTrace());
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
														processed++;
														setProgress((int)(100*processed/total));
														logger.info("progress: " + getProgress());
														
											}
										}
								}
					}
				}
					
				setDateTerminated(OffsetDateTime.now());
		
				StringBuilder str = new StringBuilder();
				str.append("Total " + String.valueOf(total));
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
	

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


}
