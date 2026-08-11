package com.novamens.content.notification;

import com.novamens.event.LogEvent;
import com.novamens.security.User;

public interface NotificationTask {
	public boolean isAlert();
	public boolean isEmail();
	public boolean isWhatsApp();
	public User getReceiver();
	public LogEvent getEvent();
	public void merge(NotificationTask task);
}
