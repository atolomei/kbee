package kbee.web.content.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EOpenFormEvent extends AbstractWicketAjaxEvent {
	
	String name;
								
	public EOpenFormEvent(AjaxRequestTarget target, String formname) {
		super(target);
		this.name = formname;
	}
	
	public String getName() {
		return this.name;
	}
}