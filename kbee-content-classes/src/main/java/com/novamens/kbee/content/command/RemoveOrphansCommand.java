package com.novamens.kbee.content.command;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;




/**
 * This is a first version. It does not work through the Scheduler.
 * It must be executed from a Wicket Session, eg in the UsersConsole.
 * 
 */
public class RemoveOrphansCommand extends AbstractCommand {
			
	
	private Serializable user_id;

	static Logger logger = LogManager.getLogger(RemoveOrphansCommand.class);

	public RemoveOrphansCommand(Serializable user_id) {
		setName("Remove Orphans userid:" + user_id);
		this.user_id=user_id;
	}
	
	@Override
	public void execute() {
		
		double total 	= 0;
		int counter	 	= 0;
		int orphan 		= 0;
		int errors 		= 0;
		int xdeleted 	= 0;
		
		try {

				setDateStarted(OffsetDateTime.now());
				
				User user = getTargetUser();
				
				if (user!=null) {
					
					List<Content> list = getContentDao().getWorkspaceContents(user, true, 0);
					total = Double.valueOf(list.size());
					
					if (total>0) {
						for (Content content: list) {
							
							if (!hasTask(content) && content.getState()==ObjectState.ENABLED) {
								
								logger.info("Orphan: " + content.getTitle());
								
								orphan++;
								try {
									content.getService(ContentService.class).delete();
									xdeleted++;
									
								} catch (ContentMgmtException e) {
									errors++;
									logger.error(e.getClass().getSimpleName());
									
								} catch (ServiceNotFoundException e) {
									errors++;
									logger.error(e.getClass().getSimpleName());
								}
							}
							else 
								logger.debug(content.getTitle() + ": ok");
							
							counter++;
							setProgress(Double.valueOf(Double.valueOf(counter) * 100.0 / total).intValue());
						}
					}
				}
				
				setProgress(100);
				setResult("Ok");
		
				StringBuilder str = new StringBuilder();
				
				str.append("Processed: "+ String.valueOf(total)+ " | ");
				str.append("Orphans: " + String.valueOf(orphan)+ " | ");
				str.append("Orphans deleted: "+ String.valueOf(errors)+ " | ");
				str.append("Errors: "+ String.valueOf(xdeleted)) ;
				
				logger.info(str.toString());
				
				setResultComments(str.toString());
				setState(CommandState.COMPLETED);
				
		} finally {

			setDateTerminated(OffsetDateTime.now());
			
		}
	}
	
	private boolean hasTask(Content content) {
		return content.getService(WorkflowService.class).getTask()!=null;
	}

	private User getTargetUser() {

		if (this.user_id==null)
			return null;
		
		return getSecurityDao().findUserById((Long) this.user_id);
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

}
