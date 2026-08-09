package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class MyListsEmptyListEvent extends AbstractWicketAjaxEvent {

	public MyListsEmptyListEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
