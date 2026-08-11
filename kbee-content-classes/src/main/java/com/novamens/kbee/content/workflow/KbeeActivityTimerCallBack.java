package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailService;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.lock.ValueLockerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.timer.CallBack;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowEvent;

import kbee.email.EmailBuilderWorkflowTaskPastDueDate;
			
public class KbeeActivityTimerCallBack implements CallBack, Serializable {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeActivityTimerCallBack.class.getName());
	
	private long processId;
	private Serializable contentId;
	private OffsetDateTime startTime;

	public KbeeActivityTimerCallBack(Activity activity) {
		setActivity(activity);
	}
	
	
	
	@Override
	public void execute() {

		Activity activity = getActivity();
		
		if (activity==null || !activity.isRunning()) {
			return;
		}
		
		Content content = null;
		
		try {
			
			content = getContent();
			
			if (content == null) {
				return;
			}
			
			lock(content);
			
			authenticate(getRootUser(content.getDomain()));
			
			activity = getActivity();
			
			if (activity==null || !activity.isRunning()) {
				return;
			}
			
			TimeOutEndCondition condition = getCondition(activity);
			
			if (condition!=null) {
				
				WorkflowEvent event = new KbeeWorkflowEvent(condition.getEvent(), condition.getLabel());
				WorkflowService ws = content.getService(WorkflowService.class);
				KbeeContext context = (KbeeContext)ws.getContext();
				
				KbeeUser current_user = (KbeeUser) context.getUser();
				Task task = context.getTask();
				
				context.setNote(condition.getNote());
				
				ws.handle(event, context);
				
				logger.debug("Sending email on Task Timeout -> " + task.getDisplayName() + " | " + content.getDisplayName() + " | " + current_user.getUserName());
				
				if (current_user != null) {
					Person notification_receiver = getContentDao().findUserProfileByUserId(current_user.getId()).getPerson();
					EmailBuilderWorkflowTaskPastDueDate builder =	new EmailBuilderWorkflowTaskPastDueDate(context, content, notification_receiver);
					ServiceLocator.getService(EmailService.class).send(builder);
					builder.setLanguage(notification_receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
					
					// ServiceLocator.getService(EmailService.class).sendTaskTimeout(context, content, current_user);
				}
			}
		}
		finally {
			if (content!=null)
				unlock(content);
		}
	}
	
	// No tiene id al momento del set
	public void setActivity(Activity activity) {
		contentId = ((KbeeContext)activity.getContext()).getContent().getId();
		startTime = activity.getStartTime();
		processId = activity.getProcess().getId();
	}
	
	public Activity getActivity() {
		Content content = getContent();
		WorkflowService ws = content.getService(WorkflowService.class);
		KbeeContext context = (KbeeContext)ws.getContext();
		com.novamens.workflow.Process process = context.getProcess();
		if (process!=null && process.getId().equals(processId))
			for (Activity activity : process.getActivities()) {
				if (activity!=null && ((KbeeWorkflowActivity)activity).getStartTime().equals(startTime))
					return activity;
			}
		return null;
	}
	
	public Content getContent() {
		return getContentDao().findContentById(contentId);
	}
	
	private void authenticate(String username) {
		if (ServiceLocator.getService(SecurityService.class).getSessionUser()==null || 
				!username.equals(ServiceLocator.getService(SecurityService.class).getSessionUser().getName())) 
		ServiceLocator.getService(SecurityService.class).authenticate(username);
	}
	
	private String getRootUser(Domain domain) {
		return "root@"+domain.getName().toLowerCase();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private void lock(Content content) {
		ServiceLocator.getService(ValueLockerService.class).lock(content.getId());
	}
	
	private void unlock(Content content) {
		ServiceLocator.getService(ValueLockerService.class).unlock(content.getId());
	}
	
	private TimeOutEndCondition getCondition(Activity activity) {
		if (!(activity.getTask() instanceof UserTask)) return null;
		for (EndCondition condition : ((UserTask)activity.getTask()).getEndConditions()) {
			if (condition instanceof TimeOutEndCondition) {
				return (TimeOutEndCondition)condition;
			}
		}
		return null;
	}
}
