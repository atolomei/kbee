package kbee.web.workflow.task;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class WorkflowPriorityEvent extends AbstractWicketAjaxEvent {
	
	public WorkflowPriorityEvent(AjaxRequestTarget target) {
			super(target);
	}
}
