package kbee.web.searcher.searchform;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class SearcherOnChangeEvent extends AbstractWicketAjaxEvent {

	private Map<String, Object> parameters;
	
	public SearcherOnChangeEvent(AjaxRequestTarget requestTarget, Map<String, Object> parameters) {
		super(requestTarget);
		this.parameters=parameters;
	}

	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
}
