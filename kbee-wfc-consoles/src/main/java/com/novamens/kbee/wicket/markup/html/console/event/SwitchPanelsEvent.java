package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class SwitchPanelsEvent extends AbstractWicketAjaxEvent {

	public SwitchPanelsEvent(AjaxRequestTarget target) {
		super(target);
	}
}
