package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class OnSearchEvent extends AbstractWicketAjaxEvent {

	String text;
	
	public OnSearchEvent(AjaxRequestTarget requestTarget, String text) {
		super(requestTarget);
		this.text=text;
	}
	
	public String getText() {
		return text;
	}
}
