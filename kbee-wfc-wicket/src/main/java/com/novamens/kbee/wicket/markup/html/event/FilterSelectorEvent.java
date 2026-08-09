package com.novamens.kbee.wicket.markup.html.event;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class FilterSelectorEvent extends AbstractWicketAjaxEvent {

	private Map<String, Object> filters;
	
	
	public FilterSelectorEvent(AjaxRequestTarget requestTarget, Map<String, Object> filters) {
		super(requestTarget);
		setFilters(filters);
	}
	
	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}
	
	public Map<String,  Object> getFilters() {
		return filters;
	}
}