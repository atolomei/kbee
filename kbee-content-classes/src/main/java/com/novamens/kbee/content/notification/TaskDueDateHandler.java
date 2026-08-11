package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.novamens.content.base.Content;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserProfile;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.logging.DueDateAlertEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderPublishEventENotiRule;
import kbee.email.EmailBuilderWorkflowDueDateTask;
import kbee.util.logging.Logger;

public class TaskDueDateHandler extends AbstractLogEventNotificationHandler  {
	
	private static Logger logger = Logger.getLogger(EmailBuilderPublishEventENotiRule.class.getName());

	@Override
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof DueDateAlertEvent))
				return notifications;
			DueDateAlertEvent taskevent = (DueDateAlertEvent) event;
			Content content = (Content)taskevent.getContent(); 
			for (User user: getMonitors(content)) {
				if (isEnabled(user)) { 
 					notifications.add(getNotification(event, user, isAlertEnabled(user), isMailEnabled(user)));
				}
			}	
		}	
		catch (Exception e) {
			logger.error(e);
		}
		return notifications;
	}
	
	@Override
	protected void execute(NotificationTask notification) {
		LogEvent event = notification.getEvent();

		if (event.isSilentMode() || !(event instanceof DueDateAlertEvent) || !notification.isEmail())
			return;
		
		User user = notification.getReceiver();
		
		if (!isEnabled(user))
			return;
		
		EmailBuilderWorkflowDueDateTask builder = new EmailBuilderWorkflowDueDateTask(((DueDateAlertEvent)event).getActivity());
		builder.setLanguage(user.getLocale().getLanguage());
		builder.setReceiver(user);
		
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
	@Override
	protected boolean isEnabled(User user) {
		if (!super.isEnabled(user)) return false;
		UserProfile profile = getProfile(user);
		return profile!=null && profile.isEmailNotifications();
	}	
	
	protected Set<User> getMonitors(Content content) {
		return getUsers(ServiceLocator.getService(ContentSystemSecurityService.class).getMonitors(content));
	}
}