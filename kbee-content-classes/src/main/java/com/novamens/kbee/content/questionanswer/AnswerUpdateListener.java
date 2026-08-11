package com.novamens.kbee.content.questionanswer;

import com.novamens.content.questionanswer.Answer;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.service.ServiceLocator;
import com.novamens.event.AppUpdateEvent;

public class AnswerUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return event.getObject() instanceof Answer;
	}
	
	public void onEvent(Event event) {
		ServiceLocator.getService(EventService.class).fire(new AppUpdateEvent(((Answer)event.getObject()).getQuestion()));
	}
}
