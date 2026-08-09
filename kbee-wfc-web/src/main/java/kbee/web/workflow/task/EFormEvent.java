package kbee.web.workflow.task;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EFormEvent extends AbstractWicketAjaxEvent {
	
	String name;
								
	public EFormEvent(AjaxRequestTarget target, String formname) {
		super(target);
		this.name = formname;
	}
	
	public String getName() {
		return this.name;
	}
}