package com.novamens.kbee.wicket.markup.html.event;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class GeneralWicketAjaxEvent extends AbstractWicketAjaxEvent {

	Map<String, Object> map;
	String name;
	
	public GeneralWicketAjaxEvent(AjaxRequestTarget requestTarget, String name) {
		super(requestTarget);
			this.name=name;
	}

	public GeneralWicketAjaxEvent(AjaxRequestTarget requestTarget, String name, Map<String, Object> map) {
		super(requestTarget);
			this.name=name;
			this.map=map;
	}
	
	public Map<String, Object> getParameters() {
		return map;
	}
	
	public String getName() {
		return this.name;
	}
	
}
