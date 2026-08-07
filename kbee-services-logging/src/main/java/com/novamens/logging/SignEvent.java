package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.content.form.EFormData;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("SignEvent")
public class SignEvent extends ContentEvent {

	
	public SignEvent() {
	}
	
	public SignEvent(Content content, EFormData data) {
		super();
		setContent(content);
		setParameters(data.getForm().getDisplayName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String getEventType() {
		return "Sign";
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
		return "Sign";
	}
}
