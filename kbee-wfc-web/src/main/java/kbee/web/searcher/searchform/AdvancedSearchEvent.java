package kbee.web.searcher.searchform;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class AdvancedSearchEvent extends AbstractWicketAjaxEvent {
			
	private Map<String, Object> filters;
	
	public AdvancedSearchEvent(AjaxRequestTarget target, Map<String, Object> filters) {
		super(target);
		this.filters = filters;
	}
	
	public Map<String, Object> getFilters() {
		return filters;
	}
	
}
