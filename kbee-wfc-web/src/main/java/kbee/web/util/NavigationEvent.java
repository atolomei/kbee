package kbee.web.util;

import java.time.Instant;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.event.Event;

public class NavigationEvent implements Event {
	
	private AjaxRequestTarget target;
	
	public NavigationEvent() {
		
	}
	
	public NavigationEvent(AjaxRequestTarget target) {
		setTarget(target);
	}

	public Object getObject() {
		return null;
	}
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}

	public AjaxRequestTarget getTarget() {
		return target;
	}

	public void setTarget(AjaxRequestTarget target) {
		this.target = target;
	}
}
