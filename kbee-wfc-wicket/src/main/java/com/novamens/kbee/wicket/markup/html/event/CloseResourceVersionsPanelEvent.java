package com.novamens.kbee.wicket.markup.html.event;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class CloseResourceVersionsPanelEvent<T> extends AbstractWicketAjaxEvent {

	Serializable id;
	
	public CloseResourceVersionsPanelEvent(AjaxRequestTarget requestTarget, Serializable id) {
		super(requestTarget);
		this.id=id;
	}

	public Serializable getId() {
		return this.id;
	}
}
