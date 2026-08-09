package kbee.web.dashboard;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class SortWicketAjaxEvent extends AbstractWicketAjaxEvent {
			
	String criteria;
	
	
	public SortWicketAjaxEvent(AjaxRequestTarget requestTarget, String criteria) {
		super(requestTarget);
			this.criteria=criteria;
	}

	
	public String getCriteria() {
		return this.criteria;
	}
}
