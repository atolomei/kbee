package com.novamens.content.notification;

import java.util.List;

import com.novamens.event.LogEvent;

public interface LogEventNotificationHandler {
	public List<NotificationTask> getNotifications(LogEvent event);
	public void notify(List<NotificationTask> notifications);
}