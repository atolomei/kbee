package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

/**
 * click "Advanced search" link
 * toggle up/down
 * 
 * @see AbstractConsole
 * 
 */
public class TopPanelEvent extends AbstractWicketAjaxEvent implements Event {

	public TopPanelEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
