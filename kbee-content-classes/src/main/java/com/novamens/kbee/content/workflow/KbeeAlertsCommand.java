package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.security.acl.KbeeGroupProxy;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.email.EmailBuilderWorkflowAlertTimeOut;
import kbee.util.logging.Logger;

public class KbeeAlertsCommand extends AsyncCommand {
		
	private static Logger logger = Logger.getLogger(KbeeAlertsCommand.class.getName());

	public KbeeAlertsCommand() {
		setName("Workflow Alerts Command");
	}
	
	public void executeAsync() {
		try {
	
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			com.novamens.hibernate.session.Session.open();
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			
			ResultSet resultSet = getActivities();
			
			logger.debug("Activities " + String.valueOf(resultSet.size()));
			
			while (resultSet.hasNext()) {
				
				Content content = (Content)resultSet.next().getObject();
				
				if (content!=null) {
				
						WorkflowContext context = content.getService(WorkflowService.class).getContext();
						
						Task task = getTask(content);
						
						if (task!=null && task instanceof KbeeTask && ((KbeeTask)task).getMaxTimePending()>0) {
							if (isMaxTimePending(content)) {
								logger.debug("max-time-pending -> " + content.getDisplayName() + " (" + content.getId().toString() +")" );
								sendAlert(context, content, EmailBuilder.MAX_TIME_PENDING);
							}
						}
						
						if (task!=null && task instanceof KbeeTask && ((KbeeTask)task).getMaxTimeRunning()>0) {
							if (isMaxTimeRunning(content)) {
								logger.debug("max-time-running -> " + content.getDisplayName() + " (" + content.getId().toString() +")" );
								sendAlert(context, content, EmailBuilder.MAX_TIME_RUNNING);
							}
						}
					
				}
			}
			
			end();
		}
		catch (Exception e) {
			logger.error(e);
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	private ResultSet getActivities() {
		ActivitiesQuery query = new ActivitiesQuery(getQueryIndex());
		ResultSet resultSet = query.execute();
		return resultSet;
	}
	
	private void sendAlert(WorkflowContext context, Content content, String alertkey) {
		if (!content.getService(ContentService.class).getAlerts().contains(alertkey)) {
			Transaction transaction = null;
			try {
				transaction = beginTransaction();
				for (Principal principal : getReceivers(content)) {
					if (principal instanceof Group) {
						for (Principal member : ((KbeeGroupProxy)principal).getMembers()) {
							if (member instanceof User) {
								Task task = getTask(content);
								String subject = getText(alertkey, ((User)member).getLocale(), task.getDisplayName(), String.valueOf(((KbeeTask)task).getMaxTimePending()));
								notifyByAlert(context, content, subject, (User) member, alertkey);
								notifyByMail(context, content,   (User) member, alertkey);

							}
						}
					}
					else {
						if (principal instanceof User) {
							Task task = getTask(content);
							String subject = getText(alertkey, ((User) principal).getLocale(), task.getDisplayName(), String.valueOf(((KbeeTask)task).getMaxTimePending()));
							notifyByAlert(context, content, subject, ((User) principal), alertkey);
							notifyByMail(context, content, ((User) principal), alertkey);
						}
					}
				}
				content.getService(ContentService.class).setAlert(alertkey);
				transaction.commit();
			}
			catch (Exception e) {
				transaction.rollback();
				logger.error(e);
				throw e;
			}
		}
	}
	
	private void notifyByAlert(WorkflowContext context, Content content, String subject, User receiver, String alertkey) {
		ServiceLocator.getService(NotificationService.class).sendNotification(NotificationType.WORKFLOW, content, subject, receiver);
	}
	
	private void notifyByMail(WorkflowContext context, Content content,   User ureceiver, String alertkey) {
		try {
			Person receiver = getPerson(ureceiver);
			EmailBuilderWorkflowAlertTimeOut builder =new EmailBuilderWorkflowAlertTimeOut(context, content,  receiver, alertkey);
			
			builder.setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			
			ServiceLocator.getService(EmailService.class).send(builder);
		} catch (Exception e) {
			logger.error(e);
		}
		
	}

	
	private boolean isMaxTimePending(Content content) {
		OffsetDateTime tasktime = content.getLastModifiedOffsetDateTime();
		Task task = getTask(content);
		if (((KbeeTask)task).getMaxTimePending()<=0)
			return false;
		if (!getWorkflowUser(content.getDomain()).equals(String.valueOf(content.getWorkspace()))) 
			return false;
		OffsetDateTime limittime = tasktime.plusDays(((KbeeTask)task).getMaxTimePending());
		OffsetDateTime today = OffsetDateTime.now();
		if (today.isAfter(limittime)) {
			return true;
		}
		return false;
	}
	
	private boolean isMaxTimeRunning(Content content) {
		OffsetDateTime tasktime = content.getLastModifiedOffsetDateTime();
		Task task = getTask(content);
		if (((KbeeTask)task).getMaxTimeRunning()<=0)
			return false;
		if (!getWorkflowUser(content.getDomain()).equals(String.valueOf(content.getWorkspace()))) 
			return false;
		OffsetDateTime limittime = tasktime.plusDays(((KbeeTask)task).getMaxTimeRunning());
		OffsetDateTime today = OffsetDateTime.now();
		if (today.isAfter(limittime)) {
			return true;
		}
		return false;
	}
	
	private Task getTask(Content content) {
		WorkflowService workflowService = content.getService(WorkflowService.class);
		Task task = workflowService.getTask();
		return task;
	}
	
	private List<Principal> getReceivers(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).getEnabledPrincipals(content, getMonitorPermission(content));
	}
	
	private Permission getMonitorPermission(Content content) {
		String permissionname = String.valueOf(content.getService(WorkflowService.class).getContext().getProcedure().getId())	+ "-" + KbeePermission.MONITOR.toString();
		return KbeePermission.valueOf(permissionname);
	}
	
	private Person getPerson(User user) {
		Person person = null;
		UserProfile userProfile = getContentDao().findUserProfileByUser(user);
		if (userProfile!=null) {
			person = (Person)userProfile.getEntity();
		}
		return person;
	}
	
	private String getText(String key, Locale locale, String... parameter) {
		try {
			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), locale);
			String text = "";
			if (resources!=null) {
				text = resources.getString(key);
			}
			if (text!=null) {
				for (int p = 0; p<parameter.length; p++) {
					text = text.replace("{"+String.valueOf(p)+"}", parameter[p]);
				}
			}
			return text;
		}
		catch (MissingResourceException e) {
		}
		return null;
	}
	
	
	//private User getSessionUser() {
	//	return ServiceLocator.getService(SecurityService.class).getSessionUser();
	//}
	
	private String getWorkflowUser(Domain domain) {
		User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(DomainService.WORKFLOW_USER+"@"+domain.getName());
		return String.valueOf(user.getId());
	}
	
	private Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}