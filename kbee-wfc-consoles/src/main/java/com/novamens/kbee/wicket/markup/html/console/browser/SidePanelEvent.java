package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class SidePanelEvent extends AbstractWicketAjaxEvent implements Event {
	public SidePanelEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
}
