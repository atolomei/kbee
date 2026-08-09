package com.novamens.kbee.wicket.markup.html.console.panel;

import java.time.Instant;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class AfterUploadEvent extends AbstractWicketAjaxEvent   {

	AjaxRequestTarget requestTarget;

	public AfterUploadEvent( AjaxRequestTarget requestTarget ) {
		super(requestTarget);
		this.requestTarget = requestTarget;
	}
	
	
	public AjaxRequestTarget getRequestTarget() {
		return requestTarget;
	}
	
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}
	
	public Object getObject() {
		return null;
	}
	
	
}
