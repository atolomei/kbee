package com.novamens.kbee.content.notification;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.CheckinEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderPublishEventENotiRule;

/**
 * 
 * Publish Content
 * 
 * Email  
 * -----
 * User is ObjectState.ENABLED
 * User has Workflow Alers Punlish enabled,  
 * 
 * Bell Alert
 * ----------
 * Yes
 * 
 * Billboard Alert
 * ---------------
 * Never
 * 
 *
 */
@Deprecated
public class PublishNotificationTaskServiceRequest extends NotificationTaskServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PublishNotificationTaskServiceRequest.class.getName());

	public PublishNotificationTaskServiceRequest(LogEvent event) {
		super(event);
		setName("Publish Notification Task:  " + event!=null?event.getDisplayName():"");
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		if (getEvent()!=null) {
			if ( getEvent() instanceof CheckinEvent) {
				Content content = (Content) ((CheckinEvent) getEvent()).getContent();
				if (content!=null)
					str.append(" | " + content.getTitle() + " | " + content.getIdInfo());
			}
		}
		return str.toString();
	}
	
	
	@Override
	protected void notify(LogEvent event) {
		try {
			if (! (event instanceof CheckinEvent))
				return;
			if (event.isSilentMode())
				return;
			
			Content content = (Content) ((CheckinEvent)event).getContent();
				
			for (ENotiRule rule: getRules(((CheckinEvent)event).getDomain(), ENotiRule.EVENT_PUBLISH_CONTENT)) {
				try {
					if (rule.evaluate(event)) {
						logger.debug("rule -> " + rule.getDisplayName() + " | condition -> " + rule.getDisplayCondition() + " | event -> " + event.getDisplayName() +" | evaluate(true)");
						for (Principal principal_subscriber: rule.getReceivers()) {
							if	(principal_subscriber instanceof User)	
								sendNotification((User) principal_subscriber, content, rule, event);
							else if (principal_subscriber instanceof Group)	
								sendNotification((Group) principal_subscriber, content, rule, event);
						}
						for (Role role: rule.getRoleReceivers()) {
							sendNotification(role, content, rule, event);
						}
					}
				}	
				catch (Exception e) {
					logger.error(e);
				}
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	
	 
		
	/**
	 * 
	 *  User  = ENABLED
	 *  User  = Receive Email Notifications TRUE 
	 * 
	 * 
	 * 
	 * @param user
	 * @param content
	 * @param rule
	 * @param event
	 */
	protected void sendNotification(User user, Content content, ENotiRule rule, LogEvent event) {
		
		if (user==null || content==null || rule==null || event==null) {
			logger.error("Mandatory parameter is null ");
			return;
		}
		
		if (!user.isEnabled() || ((KbeeUser) user).getState()!=ObjectState.ENABLED)
			return;
			
		UserProfile profile =  getContentDao().findUserProfileByUser(user);

		if (profile==null)
			return;
		
		
		if (profile.isEmailRuleNotifications()) {
				
			/** Check that the principal has READ permission on the Content
			If you can't read it then no notification is sent.
			The validity of the document is also checked. if it is not valid, it is not sent
			A document with a valid specification is considered current
			*/
			if (ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content, user) && content.getService(ContentService.class).isValid()) {
				
				try {
					/** publisher */
					Person publisher = getContentDao().findUserProfileByUser(((CheckinEvent)event).getEventUser()).getPerson();

					/** subscriber */
					Person subscriber = getContentDao().findUserProfileByUser(user).getPerson();
							
					logger.debug("Publish_" + event.getId().toString() + " - to: " + (subscriber!=null?subscriber.getDisplayName():"null") + " Rule: " + rule.getId().toString());

					if (subscriber!=null) {
						if (rule.isAlert()) {
							ServiceLocator.getService(NotificationService.class).sendPublishNotification(content, user);
						}
					}
					if (rule.isEmail()) {
						logger.debug("rule -> " + rule.getDisplayName() + " | content -> " + content.getDisplayName() + " |  publisher -> " +publisher.getDisplayName() + " | receiver -> " + subscriber.getDisplayName());
						EmailBuilderPublishEventENotiRule builder =  new EmailBuilderPublishEventENotiRule(rule, content, publisher, subscriber);
						ServiceLocator.getService(EmailService.class).send(builder);
						
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
				
			}
		}
	}
	
	protected void sendNotification(Role role, Content content, ENotiRule rule, LogEvent event) {
		for (User user : getUsers(role, content)) {
			sendNotification(user, content, rule, event);
		}
	}
	
	/**
	 * 
	 * @param domain
	 * @param eventType
	 * @return
	 * Returns Domain Rules that are ENABLED and for event event_type (both personal and system)
	 * 
	 */
	private List<ENotiRule> getRules(Domain domain, int eventType) {
		return ServiceLocator.getService(ENotiRuleService.class).getEmailRules(domain, eventType);
	}
	
	private Set<User> getUsers(Role role, Content content) {
		Set<User> users = new HashSet<User>();

		if(role instanceof EntityRole) {
			Classifier classifier = ((EntityRole) role).getClassifier();
			for (Classification classification : content.getClassification()) {
				if (classification.getClassifier().equals(classifier)) {
					users.addAll(getUsers(role, classification.getDataSetMember()));
					break;
				}
			}
		}else{
			final List<UserRole> userRolesByRole = getSecurityDao().findUserRolesByRole(role);
			users.addAll(userRolesByRole.stream().map(usR -> usR.getUser()).collect(Collectors.toList()));
		}
		return users;
	}
	
	private Set<User> getUsers(Role role, DataSetMember member) {
		Set<User> users= new HashSet<User>();
		if (!(member instanceof EntityMember)) return users;
		EntityMember entity = (EntityMember)member;
		for (UserRole userRole :  getSecurityDao().findUserRolesByEntityMember(entity)) {
			if (role==null || role.equals(userRole.getRole())) {
				users.add(userRole.getUser());
			}
		}
		return users;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
