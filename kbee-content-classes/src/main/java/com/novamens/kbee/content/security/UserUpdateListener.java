package com.novamens.kbee.content.security;

import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.AppUpdateEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;

public class UserUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent) && event.getObject() instanceof KbeeUser);
	}
	
	public void onEvent(Event event) {
		ServiceLocator.getService(ContentSystemSecurityService.class).onUpdate((KbeeUser)event.getObject());
	}
}
