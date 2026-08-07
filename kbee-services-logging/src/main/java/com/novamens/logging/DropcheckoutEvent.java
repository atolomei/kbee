package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("DropcheckoutEvent")
public class DropcheckoutEvent extends ContentEvent {
	 
	
	public DropcheckoutEvent() {
	}

	public DropcheckoutEvent(Content content) {
		super();
		setContent(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	
	
	// Deprecated
	@Override
	public String getEventType() {
		return "Drop Checkout";
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
		return "Drop checkout";
	}


}
