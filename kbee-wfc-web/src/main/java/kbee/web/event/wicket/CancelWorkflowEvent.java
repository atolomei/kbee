package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class CancelWorkflowEvent extends AbstractWicketAjaxEvent {

	public CancelWorkflowEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
	
}
