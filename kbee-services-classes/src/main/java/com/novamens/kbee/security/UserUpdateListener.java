package com.novamens.kbee.security;

import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.event.AppUpdateEvent;

public class UserUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent) && event.getObject() instanceof KbeeUser);
	}
	
	public void onEvent(Event event) {
		((KbeeSecurityService)ServiceLocator.getService(SecurityService.class)).onUpdate((KbeeUser)event.getObject());
	}
}
