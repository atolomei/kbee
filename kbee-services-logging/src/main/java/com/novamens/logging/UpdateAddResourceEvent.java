package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.content.resource.KBFile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

@Entity
@DiscriminatorValue("UpdateAddResourceEvent")
public class UpdateAddResourceEvent extends UpdateEvent {

	//@Column(name = "event_resource_id")
	//private long event_resource_id;
	
	static public String getClassEventType() {
		return "UpdateAddResource";
	}
	
	public UpdateAddResourceEvent() {
	}
	
	public UpdateAddResourceEvent(Content content, KBFile resource) {
		super(content);
		setResource(resource);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public UpdateAddResourceEvent(Content content,  KBFile resource, String description) {
		super(content);
		setContent(content);
		setResource(resource);
		setParameters(description);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		Activity activity = content.getService(WorkflowService.class).getActivity();
		if (activity!=null)
			setActivityId(activity.getId());
	}
	
	public UpdateAddResourceEvent(Content content,  KBFile resource, List<String> updatedParts) {
		super(content);
		setContent(content);
		setResource(resource);
		setParameters(getDescription(updatedParts));
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		
		Activity activity = content.getService(WorkflowService.class).getActivity();
		if (activity!=null)
			setActivityId(activity.getId());
		
	}
	
	@Override
	public String toString() {
		return getAction() + " | " + getTarget();
	}
	
	@Override
	public String getAction() {
		return "UpdateAddResource";
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
