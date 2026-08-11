package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.novamens.content.base.Content;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.logging.ProgressNoteEvent;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.ActivityProgressNote;

import kbee.email.EmailBuilderTaskProgressNote;
import kbee.util.logging.Logger;

public class TaskProgressNoteNotificationHandler extends AbstractLogEventNotificationHandler  {
	
	private static Logger logger = Logger.getLogger(TaskProgressNoteNotificationHandler.class.getName());
	
	@Override
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof ProgressNoteEvent))
				return notifications;
			ActivityProgressNote note = ((ProgressNoteEvent)event).getNote();
			Content content = ((KbeeWorkflowActivity)note.getActivity()).getContent(); 
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
		
		if (!(event instanceof ProgressNoteEvent)) {
			return;
		}
		
		User receiver = notification.getReceiver();
		
		if (!isEnabled(receiver)) 
			return;
		
		ActivityProgressNote note = ((ProgressNoteEvent)event).getNote();
		
		if (notification.isEmail()) {
			EmailBuilderTaskProgressNote builder = new EmailBuilderTaskProgressNote(note);
			builder.setLanguage(receiver.getLocale().getLanguage());
			builder.setReceiver(receiver);
			ServiceLocator.getService(EmailService.class).send(builder);
		}
		
		if (notification.isAlert()) {
			schedule(new TaskProgressNoteNotificationSender(note, receiver));
		}
	}
	
//	protected boolean isMailEnabled(User user) {
//		if (!super.isEnabled(user)) return false;
//		UserProfile profile = getProfile(user);
//		return profile!=null && profile.isEmailProgressNoteNotifications();
//	}
//	
//	protected boolean isAlertEnabled(User user) {
//		if (!super.isEnabled(user)) return false;
//		UserProfile profile = getProfile(user);
//		return profile!=null && profile.isAlertProgressNoteNotifications();
//	}
	
	protected void schedule(ServiceRequest request) {
		try {
			ServiceLocator.getService(SchedulerService.class).enqueue(request);
		}
		catch (SchedulerException e) {
			logger.error(e);
		}
	}
	
	protected Set<User> getMonitors(Content content) {
		return getUsers(ServiceLocator.getService(ContentSystemSecurityService.class).getReaders(content));
//		return getUsers(ServiceLocator.getService(ContentSystemSecurityService.class).getMonitors(content));
	}
}  