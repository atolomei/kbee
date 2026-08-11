package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.notification.NotificationTask;
import com.novamens.content.user.UserProfile;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.logging.TaskStartEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderPublishEventENotiRule;
import kbee.email.EmailBuilderWorkflowTaskAssigned;
import kbee.util.logging.Logger;

public class TaskStartNotificationHandler extends AbstractLogEventNotificationHandler  {
	
	private static Logger logger = Logger.getLogger(EmailBuilderPublishEventENotiRule.class.getName());

	@Override
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof TaskStartEvent))
				return notifications;
			TaskStartEvent taskevent = (TaskStartEvent) event;
			if (!taskevent.getEventUser().equals(taskevent.getTriggerUser())) {
				if (isEnabled(taskevent.getEventUser())) {
					notifications.add(getNotification(event, taskevent.getEventUser(), false, true));
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

		if (event.isSilentMode() || !(event instanceof TaskStartEvent) || !notification.isEmail())
			return;
		
		User user = notification.getReceiver();
		
		if (!isEnabled(user))
			return;
		
		EmailBuilderWorkflowTaskAssigned builder = new EmailBuilderWorkflowTaskAssigned(((TaskStartEvent)event).getActivity());
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
}