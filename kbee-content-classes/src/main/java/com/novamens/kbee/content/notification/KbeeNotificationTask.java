package com.novamens.kbee.content.notification;

import com.novamens.content.notification.NotificationTask;
import com.novamens.event.LogEvent;
import com.novamens.security.User;

public class KbeeNotificationTask implements NotificationTask {
	
	private LogEvent event;
	private User receiver;
	private boolean isAlert = false;
	private boolean isWhatsApp = false;
	private boolean isEmail = false;
	
	public LogEvent getEvent() {
		return event;
	}
	
	public void setEvent(LogEvent event) {
		this.event = event;
	}
	
	public User getReceiver() {
		return receiver;
	}
	
	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}
	
	public boolean isAlert() {
		return isAlert;
	}
	
	public void setAlert(boolean isAlert) {
		this.isAlert = isAlert;
	}
	
	public boolean isEmail() {
		return isEmail;
	}
	
	public void setEmail(boolean isEmail) {
		this.isEmail = isEmail;
	}
	
	public boolean isWhatsApp() {
		return isWhatsApp;
	}

	public void setWhatsApp(boolean isWhatsApp) {
		this.isWhatsApp = isWhatsApp;
	}

	public void merge(NotificationTask task) {
		if (task.isAlert()) setAlert(true);
		if (task.isEmail()) setEmail(true);
		if (task.isWhatsApp()) setWhatsApp(true);
	}
}