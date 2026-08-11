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
import com.novamens.logging.TaskPendingEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderWorkflowTaskPending;
import kbee.util.logging.Logger;

public class TaskPendingNotificationHandler extends AbstractLogEventNotificationHandler  {

	private static Logger logger = Logger.getLogger(TaskPendingNotificationHandler.class.getName());

	@Override
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof TaskPendingEvent))
				return notifications;
			Content content = (Content) ((TaskPendingEvent)event).getContent();
			for (User user: getTakers(content)) {
				if (isEnabled(user) && 
					ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content, user) &&
					ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content, user)) {
					notifications.add(getNotification(event, user, false, true));
				}
			}	
		}	
		catch (Exception e) {
			logger.error(e);
		}
		return notifications;
	}
	
	public void execute(NotificationTask notification) {
		LogEvent event = notification.getEvent();

		if (event.isSilentMode() || !(event instanceof TaskPendingEvent))
			return;
		
		User user = notification.getReceiver();
		
		if (!isEnabled(user))
			return;
		
		Content content = (Content)((TaskPendingEvent)event).getContent();

		EmailBuilderWorkflowTaskPending builder = new EmailBuilderWorkflowTaskPending(content);
		builder.setLanguage(user.getLocale().getLanguage());
		builder.setReceiver(user);
		
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
	@Override
	protected boolean isEnabled(User user) {
		if (!super.isEnabled(user)) return false;
		UserProfile profile = getProfile(user);
		return profile!=null && profile.isEmailPendingNotifications();
	}	
	
	protected Set<User> getTakers(Content content) {
		return getUsers(ServiceLocator.getService(ContentSystemSecurityService.class).getTakers(content));
	}
}