package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * 
 * 
 * UpdateAddFileEvent -> [File_Id]
 * event_resource_id = 
 * 
 *
 */
@Entity
@DiscriminatorValue("UpdateEvent")
public class UpdateEvent extends ContentEvent {
 
	static public String getClassEventType() {
		return "Update";
	}
	
	public UpdateEvent() {
	}
	
	public UpdateEvent(Content content) {
		super(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public UpdateEvent(Content content, String description) {
		super(content);
		setContent(content);
		setParameters(description);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public UpdateEvent(Content content, Resource resource, String description) {
		super(content);
		setContent(content);
		setResource(resource);
		setParameters(description);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public UpdateEvent(Content content, List<String> updatedParts) {
		super(content);
		setContent(content);
		setParameters(getDescription(updatedParts));
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String toString() {
		return getAction() + " | " + getTarget();
	}
	
	@Override
	public String getAction() {
		return "Update";
	}

	@Override
	public String getEventType() {
		return  getClassEventType();
	}
	
	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "Content"; // o lo que sea !!!  VER
	}
}