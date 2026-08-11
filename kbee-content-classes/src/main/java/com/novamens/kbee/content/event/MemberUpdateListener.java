package com.novamens.kbee.content.event;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.event.EventsDispatcher;
import com.novamens.content.model.DataSetMember;
import com.novamens.event.Event;
import com.novamens.event.EventListener;

import kbee.util.logging.Logger;

import com.novamens.event.AppUpdateEvent;

public class MemberUpdateListener implements EventsDispatcher {
	
	private static Logger logger = Logger.getLogger(MemberUpdateListener.class.getName());

	private List<EventListener> listeners = new ArrayList<EventListener>();
	
	public boolean listen(Event event) {
		return event instanceof AppUpdateEvent && event.getObject() instanceof DataSetMember;
	}
	
	public void onEvent(Event event) {
		if (event!=null) {
			for (EventListener listener : listeners) {
				try {
					listener.onEvent(event);
				}
				catch (Exception e) {	
					logger.error(e);
				}
			}
		}
	}
	
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}
}
