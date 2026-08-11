package com.novamens.content.event;

import com.novamens.event.EventListener;

public interface EventsDispatcher extends EventListener {
	public void addListener(EventListener listener); 
	public void removeListener(EventListener listener); 
}
