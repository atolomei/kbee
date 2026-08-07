package com.novamens.event;

/**
 *  
 *
 */
public interface EventListener {
	public boolean listen(Event event);
	public void onEvent(Event event);
}
