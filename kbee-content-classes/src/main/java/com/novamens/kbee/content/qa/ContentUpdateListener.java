package com.novamens.kbee.content.qa;

import com.novamens.content.qa.QAService;
import com.novamens.event.BeforeUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.base.KbeeContent;

public class ContentUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return false;
		//return event instanceof BeforeUpdateEvent && event.getObject() instanceof KbeeContent;
	}
	
	public void onEvent(Event event) {
		// ((KbeeContent)event.getObject()).getService(QAService.class).update();
	}
}