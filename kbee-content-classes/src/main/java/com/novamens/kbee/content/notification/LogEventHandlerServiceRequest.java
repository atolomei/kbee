package com.novamens.kbee.content.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.content.notification.LogEventNotificationHandler;
import com.novamens.content.notification.NotificationTask;
import com.novamens.dom.ObjectID;
import com.novamens.event.LogEvent;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

public class LogEventHandlerServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private ObjectId objectId;
	
	private static Logger logger = Logger.getLogger(LogEventHandlerServiceRequest.class.getName());

	public LogEventHandlerServiceRequest(LogEvent event) {
		setEvent(event);
		try {
			setObjectID(new ObjectID(event).toString());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public void execute() {
		try {
			Map<Serializable, NotificationTask> notificationsmap = new HashMap<Serializable, NotificationTask>();
			List<NotificationTask> notifications = new ArrayList<NotificationTask>();
			List<LogEventNotificationHandler> handlers = getHandlers();
			for(LogEventNotificationHandler handler : handlers) {
				for (NotificationTask notification : handler.getNotifications(getEvent())) {
					if (notificationsmap.get(notification.getReceiver().getId())!=null) {
						notificationsmap.get(notification.getReceiver().getId()).merge(notification);
					}
					else {
						notificationsmap.put(notification.getReceiver().getId(), notification);
					}
				}
			}
			notifications.addAll(notificationsmap.values());
			for(LogEventNotificationHandler handler : handlers) {
				handler.notify(notifications);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	public void setEvent(LogEvent event) {
		objectId = new ObjectId(event);
	}
	
	public LogEvent getEvent() {
		LogEvent event = null;
		try {
			event = (LogEvent)getContentDao().findObjectById(objectId);
		} 
		catch (ContentMgmtException e) {
			logger.error(e);
		}	
		return event;
	}
	
	public List<LogEventNotificationHandler> getHandlers() {
		List<LogEventNotificationHandler> handlers = new ArrayList<LogEventNotificationHandler>();
		handlers.add(new RulesNotificationHandler());
		handlers.add(new PublishNotificationHandler());
		handlers.add(new TaskProgressNoteNotificationHandler());
		handlers.add(new TaskPendingNotificationHandler());
		handlers.add(new TaskStartNotificationHandler());
		handlers.add(new SubscriptionNotificationHandler());
		handlers.add(new TaskReassignedNotificationHandler());
		handlers.add(new WorkNoteHandler());
		handlers.add(new WhatsAppNotificationHandler());
		handlers.add(new TaskDueDateHandler());
		return handlers;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}