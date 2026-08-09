package com.novamens.kbee.wicket.markup.html.event;

import java.time.Instant;
import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.event.Event;

public class AbstractWicketAjaxEvent implements WicketAjaxEvent  {
	
	private AjaxRequestTarget requestTarget;

	public AbstractWicketAjaxEvent(AjaxRequestTarget requestTarget) {
		this.requestTarget = requestTarget;
	}
	
	public AjaxRequestTarget getRequestTarget() {
		return requestTarget;
	}
	
	public Instant getTime() {
		return Instant.now();
	}
	
	public Object getObject() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void fire(Page page) {
		for (WicketEventListener<Event> listener : page.getBehaviors(WicketEventListener.class)) {
			if (listener.handle(this)) {
				listener.onEvent(this);
			}
		}
		fire(page.iterator());
	}	
	
	@SuppressWarnings("unchecked")
	protected boolean fire(Iterator<Component> components) {
		boolean handled = false;
		while (components.hasNext()) {
			Component component = components.next();
			for (WicketEventListener<Event> listener : component.getBehaviors(WicketEventListener.class)) {
				if (listener.handle(this)) {
					listener.onEvent(this);
				}
			}
			if (!handled) {
				if (component instanceof MarkupContainer) {
					handled = fire (((MarkupContainer)component).iterator());
				}
			}
			else {
				break;
			}
		}
		return handled;
	}
	
}
