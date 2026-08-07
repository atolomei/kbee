package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import com.novamens.logging.ContentEvent;

/**
 *
 *
 */
@Entity
@DiscriminatorValue("NotificationEvent")
public class NotificationEvent extends ContentEvent {

	static public String getClassEventType() {
		return "Notify";
	}
	
	public NotificationEvent() {
	}
	
	public NotificationEvent(Content content, String subject) {
		super(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setParameters(getAction() +  " : " + subject);
	}
	
	@Override
	public String toString() {
		return  getAction()  + ". " + getTarget();
	}
	
	@Override
	public String getEventType() {
		return getClassEventType();
	}
	
	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "IDoc"; // o lo que sea !!!  VER
	}
	
	@Override
	public String getAction() {
		return "Notify";
	}
}
