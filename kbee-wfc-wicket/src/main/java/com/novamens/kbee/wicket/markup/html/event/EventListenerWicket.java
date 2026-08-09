package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.behavior.Behavior;

/**
 * 
 * 
 * This is for Wicket
 *
 * @param <T>
 */
public abstract class EventListenerWicket<T extends WicketAjaxEvent> extends Behavior {
	private static final long serialVersionUID = 1L;
	private Class<?> eventclass;
	
	public EventListenerWicket(T event) {
		this.eventclass = event.getClass();
	}
	
	public EventListenerWicket(Class<T> eventclass) {
		this.eventclass = eventclass;
	}
	
	public boolean handle(WicketAjaxEvent event) {
		return eventclass.isInstance(event);
	}
	
	public abstract void onEvent(T event);
}
