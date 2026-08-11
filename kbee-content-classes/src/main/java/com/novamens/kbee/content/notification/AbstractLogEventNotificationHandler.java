package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.notification.LogEventNotificationHandler;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeeGroupProxy;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

public abstract class AbstractLogEventNotificationHandler implements LogEventNotificationHandler {
	
	public List<NotificationTask> getNotifications(LogEvent event) {
		return new ArrayList<NotificationTask>();
	}	
	
	@Override
	public void notify(List<NotificationTask> notifications) {
		for (NotificationTask notification : notifications) {
			execute(notification);
		}
	}
	
	protected void execute(NotificationTask notification) {
	}
	
	protected NotificationTask getNotification(LogEvent event, User receiver, boolean isalert, boolean isemail) {
		KbeeNotificationTask notification = new KbeeNotificationTask();
		notification.setReceiver(receiver);
		notification.setEvent(event);
		notification.setEmail(isemail);
		notification.setAlert(isalert);
		return notification;
	}
	
	protected Set<User> getUsers(List<Principal> principals) {
		Set<User> users = new HashSet<User>();
		for (Principal principal : principals) {
			if	(principal instanceof User) {
				users.add((User)principal);
			}
			if	(principal instanceof Group) {
				users.addAll(getMembers((Group)principal));
			}
		}
		return users;
	}
	
	protected Set<User> getMembers(Group group) {
		Set<User> users = new HashSet<User>();
		if (group instanceof KbeeGroupProxy) group = ((KbeeGroupProxy)group).getGroup();
		if (group.isEnabled() && group instanceof KbeeGroup) {
			for (Principal member : ((KbeeGroup) group).getMembers()) {
				if (member instanceof User) {
					users.add((User)member);
				}
				if (member instanceof Group) {
					users.addAll(getMembers((Group)member));
				}
			}
		}
		return users;
	}
	
	protected boolean isMailEnabled(User user) {
		if (!isEnabled(user)) return false;
		UserProfile profile = getProfile(user);
		return profile!=null && profile.isEmailProgressNoteNotifications();
	}
	
	protected boolean isAlertEnabled(User user) {
		if (!isEnabled(user)) return false;
		UserProfile profile = getProfile(user);
		return profile!=null && profile.isAlertProgressNoteNotifications();
	}
	
	protected User getUser(Person person) {
		UserProfile userProfile = person.getProfile(UserProfile.class);
		return userProfile!=null ? userProfile.getUser() : null;
	}
	
	protected UserProfile getProfile(User user) {
		return getContentDao().findUserProfileByUser(user);
	}
	
	protected boolean isEnabled(User user) {
		if (user==null || !user.isEnabled() || ((KbeeUser) user).getState()!=ObjectState.ENABLED)
			return false;
		return true;
	}	
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
