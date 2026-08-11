package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notification.LogEventNotificationHandler;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.TaskStartEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.whatsapp.HsmComponent;
import com.novamens.whatsapp.HsmComponent.Section;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.whatsapp.HsmParameter;
import com.novamens.whatsapp.WhatsAppService;

import kbee.util.logging.Logger;

public class WhatsAppNotificationHandler implements LogEventNotificationHandler {
	
	private static Logger logger = Logger.getLogger(WhatsAppNotificationHandler.class.getName());

	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof TaskStartEvent))
				return notifications;
			TaskStartEvent taskevent = (TaskStartEvent) event;
			if (isEnabled(taskevent.getEventUser()) && getProfile(taskevent.getEventUser()).isWhatsAppEnabled()) {
				KbeeNotificationTask notification = new KbeeNotificationTask();
				notification.setReceiver(taskevent.getEventUser());
				notification.setEvent(event);
				notification.setWhatsApp(true);
				notifications.add(notification);
			}
		}	
		catch (Exception e) {
			logger.error(e);
		}
		return notifications;
	}	
	
	@Override
	public void notify(List<NotificationTask> notifications) {
		for (NotificationTask notification : notifications) {
			execute(notification);
		}
	}

	/**
	 * 
	 */
	protected void execute(NotificationTask notification) {
		if (!notification.isWhatsApp())
			return;
		
		LogEvent event = notification.getEvent();
		
		if (event.isSilentMode() || !(event instanceof TaskStartEvent))
			return;
		
		User user = notification.getReceiver();
				
		if (!isEnabled(user))
			return;
		
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)((TaskStartEvent)event).getActivity();
		Content content = activity.getContent();
		KbeeTask task = (KbeeTask)getTask(activity);
		
		
		
		List<HsmComponent> components = new ArrayList<>();
		HsmComponent component;
		
		List<HsmParameter> parameters;
		HsmParameter parameter;
		
		component = new HsmComponent();
		component.setSection(Section.Header);
		parameters = new ArrayList<>();
		
		parameter = new HsmParameter();
		parameter.setType("text");
		parameter.setValue(content.getDomain().getDisplayName());
		parameters.add(parameter);
		
		component.setParameters(parameters);
		components.add(component);
		
		component = new HsmComponent();
		component.setSection(Section.Body);
		parameters = new ArrayList<>();
		
		parameter = new HsmParameter();
		parameter.setType("text");
		parameter.setValue(activity.getUser().getDisplayName());
		parameters.add(parameter);

		parameter = new HsmParameter();
		parameter.setType("text");
		parameter.setValue(content.getTitle());
		parameters.add(parameter);

		parameter = new HsmParameter();
		parameter.setType("text");
		parameter.setValue(task.getDisplayName());
		parameters.add(parameter);
		
		component.setParameters(parameters);
		components.add(component);
		
		component = new HsmComponent();
		component.setSection(Section.Button);
		parameters = new ArrayList<>();

		parameter = new HsmParameter();
		parameter.setType("text");

		String urlsuffix = null;
		
		if (task.isEnablePublicLink()) {
			String url = content.getService(UrlService.class).getPublicTaskUrl();
			int i = url.lastIndexOf("/");
			urlsuffix = url.substring(i+1);
		}
		else {
			String url = content.getService(UrlService.class).getTaskUrl();
			if (url!=null) {
				int i = url.lastIndexOf("v6");
				urlsuffix = url.substring(i+3);
			}
		}
		
		if (urlsuffix!=null)
			parameter.setValue(urlsuffix);
		
		parameters.add(parameter);

		component.setParameters(parameters);
		components.add(component);
		
		String templateName = task.isEnablePublicLink() ? "new_shared_task" : "new_task";
		String phone = getProfile(activity.getUser()).getPerson().getPhone();
		
		ServiceLocator.getService(WhatsAppService.class).startConversation(templateName, phone, components);
	}
	
	/**
	 * 
	 * 
	 * @param user
	 * @return
	 */
	protected UserProfile getProfile(User user) {
		return getContentDao().findUserProfileByUser(user);
	}
	
	
	/**
	 * 
	 */
	protected boolean isEnabled(User user) {
		if (user==null || !user.isEnabled() || ((KbeeUser) user).getState()!=ObjectState.ENABLED)
			return false;
		return true;
	}

	/**
	 * 
	 */
	protected Task getTask(Activity activity) {
		for (Task task : activity.getProcess().getProcedure().getTasks()) {
			if (task.getId().equals(((KbeeWorkflowActivity)activity).getTaskName()))
				return task;
		}
		return null;
	}

	/**
	 * 
	 */
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
