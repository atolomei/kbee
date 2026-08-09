package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class MyListsAddListEvent extends AbstractWicketAjaxEvent {

	public MyListsAddListEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
