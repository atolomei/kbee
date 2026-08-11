package com.novamens.kbee.content.notification;

import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.TaskStartEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import kbee.email.EmailBuilderWorkflowTaskAssigned;
import kbee.util.logging.Logger;

@Deprecated
public class TaskStartNotificationTaskServiceRequest extends NotificationTaskServiceRequest {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TaskStartNotificationTaskServiceRequest.class.getName());

	public TaskStartNotificationTaskServiceRequest(LogEvent event) {
		super(event);
	}
	
	@Override
	protected void notify(LogEvent event) {
		try {
			if (event.isSilentMode()) 
				return;
			TaskStartEvent taskevent = (TaskStartEvent) event;
			logger.debug(event.toString());
			if (!taskevent.getEventUser().equals(taskevent.getTriggerUser())) {
				sendEmailNotification(taskevent);
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
 	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		if (getEvent()!=null) {
			Content content	= (Content) ((TaskStartEvent) getEvent()).getContent();
			if (content!=null) {
				str.append(" | Content -> " + content.getTitle() + " | " + content.getIdInfo());
			}
			str.append(" | Task -> " + ((TaskStartEvent) getEvent()).getTask());
		}
		return str.toString();
	}

	protected void sendEmailNotification(TaskStartEvent event) {
		if (event.getEventUser()==null)
			return;
		if (!event.getEventUser().isEnabled() || ((KbeeUser) event.getEventUser()).getState()!=ObjectState.ENABLED)
			return;
		//Content			 	content 			= (Content) event.getContent();
		//WorkflowContext 	context 			= content.getService(WorkflowService.class).getContext();
		Person 				task_assigned_to	= getContentDao().findUserProfileByUser(event.getEventUser()).getPerson();
		//Person 				task_trigered_by	= getContentDao().findUserProfileByUser(event.getTriggerUser()).getPerson();
		EmailBuilderWorkflowTaskAssigned builder= new EmailBuilderWorkflowTaskAssigned();
		 
		builder.setLanguage(task_assigned_to.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		
		ServiceLocator.getService(EmailService.class).send(builder);
	}
	
	@Override
	protected void sendNotification(User user, Content content, ENotiRule rule, LogEvent event) {
		throw new KbeeRuntimeException("should never be here");
		
	}
}