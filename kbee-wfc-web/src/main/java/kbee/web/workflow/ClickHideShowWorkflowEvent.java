package kbee.web.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;



public class ClickHideShowWorkflowEvent extends AbstractWicketAjaxEvent {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClickHideShowWorkflowEvent.class.getName());

	
	public ClickHideShowWorkflowEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
