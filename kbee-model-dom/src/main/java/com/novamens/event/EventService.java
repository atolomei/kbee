package com.novamens.event;

import com.novamens.service.SystemService;

public interface EventService extends SystemService {
	public void fire(Event event);
	public void addListener(EventListener listener);
}