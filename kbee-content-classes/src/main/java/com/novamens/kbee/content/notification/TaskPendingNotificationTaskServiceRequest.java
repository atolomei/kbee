package com.novamens.kbee.content.notification;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeeGroupProxy;
import com.novamens.logging.TaskPendingEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.email.EmailBuilderWorkflowTaskPending;

/**
 * 
 * We will notify
 * 
 *  - Users ENABLED
 *  - Users that can take the Task based on Roles (does not include Domain Admin)
 *  - Users that have a explicit Email Notification Rule on Pending
 *  
 */
@Deprecated
public class TaskPendingNotificationTaskServiceRequest extends NotificationTaskServiceRequest {
		
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskPendingNotificationTaskServiceRequest.class.getName());
																								
	
	public TaskPendingNotificationTaskServiceRequest(LogEvent event) {
		super(event);
		setName("Task Pending Notification");
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		if (getEvent()!=null) {
			Content content	= (Content) ((TaskPendingEvent) getEvent()).getContent();
			if (content!=null) {
				str.append(" | Content -> " + content.getTitle() + " | " + content.getIdInfo());
			}
			WorkflowContext 	context	= content.getService(WorkflowService.class).getContext();
			if (context!=null) {
				str.append(" | Task -> " + context.getCurrentActivity().getTask().getDisplayName());
			}
		}
		return str.toString();
	}
	
	
	@Override
	protected void notify(LogEvent event) {
		
		Content content = (Content) ((TaskPendingEvent)event).getContent();

		if (content==null || content.getWorkspace()==null || content.getDomain()==null)
			return;
		
		if (event.isSilentMode())
			return;

		
		/**
		 * 
		 */
		for (Principal principal: getTakers(content)) {
			try {
				if (principal instanceof User) {
					if (ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content, (User) principal)) {
						// note: if user has disabled email pending notifications -> the email will not be sent
						sendEmailNotification((TaskPendingEvent)event, (User)principal);
		 			}
				}	
				else if (principal instanceof Group)	{
					for (Principal member : ((KbeeGroupProxy)principal).getMembers()) {
						if (member instanceof User) {
							
							// isTakeable includes domain admin and root
							//
							if (ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content, (User) member)) {
								// note: if user has disabled email pending notifications -> the email will not be sent
								sendEmailNotification((TaskPendingEvent)event, (User)member);
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		

		/**
		 * 
		 * 
		 */
		for (ENotiRule rule : getRules(((TaskPendingEvent)event).getDomain(), ENotiRule.EVENT_PENDING_TASK)) {
			try {
				if (rule.evaluate(event))  {
					for (Principal principal: rule.getReceivers()) {
						if (principal instanceof User) {
							if (ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content, (User) principal)) {
								// note: if user has disabled email pending notifications -> the email will not be sent
								sendNotification((User) principal, content, rule, event);
							}
						}
						else if (principal instanceof Group)	{
							for (Principal member : ((KbeeGroup)principal).getMembers()) {
								if (member instanceof User) {
									if (ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content, (User) member)) {
										// note: if user has disabled email pending notifications -> the email will not be sent
										sendNotification((User) member, content, rule, event);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * USER Enabled
	 * USER must have Email Task Notifications = TRUE
	 * 
	 */
	protected void sendNotification(User user, Content content, ENotiRule rule, LogEvent event) {
		
		if (!user.isEnabled() || ((KbeeUser) user).getState()!=ObjectState.ENABLED)
			return;
		
		UserProfile profile =  getContentDao().findUserProfileByUser(user);

		if (profile.isEmailRuleNotifications()) {
				
			/**   
			 * Chequea que el principal tenga permiso de READ sobre el Content
		    	  Si no puede leerlo entonces no se envia notificacion.
			 */												

			if (ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content, user)) {
					try {
						logger.debug("Pending_" + event.getId().toString() + " - to: " + user.getDisplayName() + " Rule: " + rule.getId().toString());
						sendEmailNotification((TaskPendingEvent)event, user);
						
					} 
					catch (Exception e) {
						logger.error(e);
					}
			}
		}
	}
	
	protected List<Principal> getTakers(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).getTakers(content);
	}
	
	protected void sendEmailNotification(TaskPendingEvent event, User user) {
		
		Content content = (Content) event.getContent();
		 EmailBuilderWorkflowTaskPending  builder = new EmailBuilderWorkflowTaskPending(content);
		 
		 ServiceLocator.getService(EmailService.class).send(builder);
	 
		
	}
	
	protected List<ENotiRule> getRules(Domain domain, int eventType) {
		return ServiceLocator.getService(ENotiRuleService.class).getEmailRules(domain, eventType);
	}
}