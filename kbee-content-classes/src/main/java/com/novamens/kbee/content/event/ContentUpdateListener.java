package com.novamens.kbee.content.event;

import com.novamens.content.base.Content;
import com.novamens.event.AppCreateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.AppUpdateEvent;

public class ContentUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return (event instanceof AppUpdateEvent || event instanceof AppCreateEvent) && event.getObject() instanceof Content;
	}
	
	public void onEvent(Event event) {
	}
}
