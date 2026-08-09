package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface WicketAjaxEvent extends com.novamens.event.Event {
	
	public AjaxRequestTarget getRequestTarget();
}
