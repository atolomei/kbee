package com.novamens.kbee.wicket.markup.html.console.panel;

import java.time.Instant;

import com.novamens.content.query.SavedQuery;
import com.novamens.kbee.wicket.markup.html.event.WicketEvent;

public class ApplySavedQueryLinkEvent implements WicketEvent {

	
	 SavedQuery query;
	 
	public ApplySavedQueryLinkEvent(SavedQuery query) {
			this.query=query;
	}
		
	public SavedQuery getQuery() {
		return this.query;
	}

	
	@Override
	public Instant getTime() {
		return Instant.now();
	}

	@Override
	public Object getObject() {
		return  query;
	}

}
