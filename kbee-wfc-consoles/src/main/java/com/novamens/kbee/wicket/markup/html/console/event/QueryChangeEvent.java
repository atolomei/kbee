package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class QueryChangeEvent extends AbstractWicketAjaxEvent {
	private Query query;

	
	public QueryChangeEvent(AjaxRequestTarget target) {
		super(target);
	}
	
	public QueryChangeEvent(AjaxRequestTarget target, Query query) {
		super(target);
		this.query = query;
	}
	
	public Query getQuery() {
		return query;
	}
	
}
