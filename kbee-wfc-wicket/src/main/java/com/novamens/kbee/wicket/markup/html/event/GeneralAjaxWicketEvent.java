package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class GeneralAjaxWicketEvent extends AbstractWicketAjaxEvent {

	private String name;
	
	public GeneralAjaxWicketEvent(AjaxRequestTarget requestTarget, String name) {
		super(requestTarget);
		this.name=name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public Object getObject() {
		return name;
	}
}