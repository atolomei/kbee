package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("SubscribeEvent")
public class SubscribeEvent extends ContentEvent {

	public SubscribeEvent() {
	}
	
	public SubscribeEvent(Content content) {
		super();
		setContent(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String getEventType() {
		return "Subscribe";
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
		return "Subscribe";
	}
}