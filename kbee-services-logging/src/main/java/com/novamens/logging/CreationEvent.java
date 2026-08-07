package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("CreationEvent")
public class CreationEvent extends ContentEvent {

	
	public CreationEvent() {
	}
	
	public CreationEvent(Content content) {
		super();
		setContent(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}

	
	// Deprecated
	@Override
	public String getEventType() {
		return "Create";
	}
	
	
	
	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "Content"; // o lo que sea !!!  VER
	}

	
	@Override
	public String getAction() {
		return "Create";
	}
}
