package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.indexer.query.Suggestion;

public class OnSearchSuggestionEvent extends AbstractWicketAjaxEvent {

	Suggestion suggestion;
	
	public OnSearchSuggestionEvent(AjaxRequestTarget requestTarget, Suggestion sug) {
		super(requestTarget);
		this.suggestion=sug;
	}
	
	public Suggestion getSuggestion() {
		return this.suggestion;
	}
}