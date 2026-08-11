package com.novamens.kbee.content.event;

import com.novamens.content.model.Classification;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.event.AppUpdateEvent;
import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.service.ServiceLocator;

public class ClassificationUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent || event instanceof AppCreateEvent) && event.getObject() instanceof Classification);
	}
	
	public void onEvent(Event event) {
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(((KbeeClassification)event.getObject()).getContent()));
	}
}
