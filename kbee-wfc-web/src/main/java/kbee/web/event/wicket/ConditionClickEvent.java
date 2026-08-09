package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ConditionClickEvent extends AbstractWicketAjaxEvent {

	ManualEndCondition condition;
	
	public ConditionClickEvent(AjaxRequestTarget requestTarget, ManualEndCondition condition) {
		super(requestTarget);
		this.condition=condition;
	}
	
	
	public ManualEndCondition getEndCondition() {
		return this.condition;
	}

}
