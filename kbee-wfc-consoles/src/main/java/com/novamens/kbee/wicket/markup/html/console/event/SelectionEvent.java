package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class SelectionEvent extends AbstractWicketAjaxEvent {

	public SelectionEvent(AjaxRequestTarget target) {
		super(target);
	}
}
