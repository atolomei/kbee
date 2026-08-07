package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("UnsubscribeEvent")
public class UnsubscribeEvent extends ContentEvent {

	public UnsubscribeEvent() {
	}
	
	public UnsubscribeEvent(Content content) {
		super();
		setContent(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String getEventType() {
		return "Unsubscribe";
	}
	
	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "Content";
	}

	@Override
	public String getAction() {
		return "Unsubscribe";
	}
}