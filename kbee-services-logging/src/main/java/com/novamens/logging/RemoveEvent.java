package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("RemoveEvent")
public class RemoveEvent extends ContentEvent {

	static public String getClassEventType() {
		return "Remove";
	}
	
	public RemoveEvent() {
	}
	
	public RemoveEvent(Content content) {
		super();
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setContent(content);
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
		return "Delete";
	}
}