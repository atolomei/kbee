package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.query.SavedQuery;
import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ApplySavedQueryEvent extends AbstractWicketAjaxEvent implements Event {

	 SavedQuery query;
	 
	 
	public ApplySavedQueryEvent(AjaxRequestTarget requestTarget, SavedQuery query) {
		super(requestTarget);
		this.query=query;
	}
	
	public SavedQuery getQuery() {
		return this.query;
	}

}
