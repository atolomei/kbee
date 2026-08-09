package kbee.web.workflow.task;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.content.workflow.KbeeWorkflowEvent;

public class KbeeWebWorkflowEvent extends KbeeWorkflowEvent {
	
	private AjaxRequestTarget target;
	
	public KbeeWebWorkflowEvent(String id, String label, AjaxRequestTarget target) {
		super(id, label);
		setTarget(target);
	}
	
	public void setTarget(AjaxRequestTarget target) {
		this.target = target;
	}
	
	public AjaxRequestTarget getTarget() {
		return target;
	}
}