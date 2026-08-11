package com.novamens.kbee.content.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserRole;
import com.novamens.dom.Domain;
import com.novamens.event.LogEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

public class RulesNotificationHandler extends AbstractLogEventNotificationHandler {
	
	private static Logger logger = Logger.getLogger(RulesNotificationHandler.class.getName());
	
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof ContentEvent))
				return notifications;
			
			Map<Serializable, NotificationTask> notificationsmap = new HashMap<Serializable, NotificationTask>();

			Content content = (Content) ((ContentEvent)event).getContent();
				
			for (ENotiRule rule: getRules(((ContentEvent)event).getDomain(), event)) {
				try {
					if (rule.evaluate(event)) {
						logger.debug("rule -> " + rule.getDisplayName() + " | condition -> " + rule.getDisplayCondition() + " | event -> " + event.getDisplayName() +" | evaluate(true)");
						Set<User> receivers = new HashSet<User>();
						receivers.addAll(getUsers(rule.getReceivers()));
						receivers.addAll(getUsers(rule.getRoleReceivers(), content));
						for (User receiver : receivers) {
							boolean email = rule.isEmail() && isMailEnabled(receiver);
							boolean alert = rule.isAlert() && isAlertEnabled(receiver);
							NotificationTask notification = getNotification(event, receiver, alert, email);
							if (notificationsmap.get(receiver.getId())!=null) {
								notificationsmap.get(receiver.getId()).merge(notification);
							}
							else {
								notificationsmap.put(receiver.getId(), notification);
							}
						}
					}
					
				}	
				catch (Exception e) {
					logger.error(e);
				}
			}
			
			notifications.addAll(notificationsmap.values());
		} 
		catch (Exception e) {
			logger.error(e);
		}

		return notifications;
	}
	
//	private boolean isMailEnabled(User user) {
//		return getProfile(user).isEmailRuleNotifications();
//	}
	
//	private boolean isAlertEnabled(User user) {
//		return getProfile(user).isAlertRuleNotifications();
//	}
	
	private Set<User> getUsers(List<Role> roles, Content content) {
		Set<User> users = new HashSet<User>();
		for (Role role : roles) {
			users.addAll(getUsers(role, content));
		}
		return users;
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
		}
		else {
			List<UserRole> userRolesByRole = getSecurityDao().findUserRolesByRole(role);
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
	
	private List<ENotiRule> getRules(Domain domain, LogEvent event) {
		return ServiceLocator.getService(ENotiRuleService.class).getEmailRules(domain, event);
	}
}
