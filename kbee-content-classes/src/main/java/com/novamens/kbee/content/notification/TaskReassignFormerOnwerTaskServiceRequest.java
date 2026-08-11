package com.novamens.kbee.content.notification;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.logging.TaskReassignedFormerOwnerEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderTaskReassignedFormerOwner;


@Deprecated
public class TaskReassignFormerOnwerTaskServiceRequest extends NotificationTaskServiceRequest {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskReassignFormerOnwerTaskServiceRequest.class.getName());
																								
	
	public  TaskReassignFormerOnwerTaskServiceRequest(LogEvent event) {
		super(event);
		setName("Task Reassigned Notification to former owner");
	}
	
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		if (getEvent()!=null) {
			if ( getEvent() instanceof TaskReassignedFormerOwnerEvent) {
				Content content = (Content) ((TaskReassignedFormerOwnerEvent) getEvent()).getContent();
				if (content!=null) {
					str.append(" | Content -> " + content.getTitle() + " | " + content.getIdInfo());
					TaskReassignedFormerOwnerEvent ev = (TaskReassignedFormerOwnerEvent) getEvent();
					Serializable a_id=ev.getActivityId();
					if (a_id!=null) {
						str.append(" | a_id -> " + a_id.toString());
					}
					
				}
				
				
				
			}
		}
		return str.toString();
	}

	
	@Override
	protected void notify(LogEvent event) {
		
		Content content = (Content) ((TaskReassignedFormerOwnerEvent) event).getContent();

		if (content==null || content.getWorkspace()==null || content.getDomain()==null)
			return;
		
		if (event.isSilentMode())
			return;
		
		
		logger.debug(event.toString());
		
		// send email to former user
		//
		//WorkflowContext context = content.getService(WorkflowService.class).getContext();

		//TaskReassignedFormerOwnerEvent ev = (TaskReassignedFormerOwnerEvent) event;
		//Serializable a_id=ev.getActivityId();
		//Person reassigned_by = getContentDao().findUserProfileByUser(ev.getTriggerUser()).getPerson();

		EmailBuilderTaskReassignedFormerOwner  builder = new EmailBuilderTaskReassignedFormerOwner();
		
		ServiceLocator.getService(EmailService.class).send(builder);
		
	}
	

	
	@Override
	protected void sendNotification(User user, Content content, ENotiRule rule, LogEvent event) {
	}
	


}
