package com.novamens.logging;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("ReadEvent")
public class ReadEvent extends ContentEvent {
	 
	
	static public String getClassEventType() {
		return "Read";
	}
	
	public ReadEvent() {
	}
	
	public ReadEvent(Content content) {
		super(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public ReadEvent(Content content, String... message) {
		super(content);
		setContent(content);
		List<String> messages = new ArrayList<String>(message.length);
		for (int m=0; m<message.length; m++) messages.add(message[m]);
		setParameters(getDescription(messages));
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public ReadEvent(Content content, List<String> messages) {
		super(content);
		setContent(content);
		setParameters(getDescription(messages));
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String getAction() {
		return "Read";
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
		return "IDoc"; // o lo que sea !!!  VER
	}
}
