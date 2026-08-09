package kbee.web.workflow.task;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormField;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ValidationEvent extends AbstractWicketAjaxEvent {
	
	EForm form;
	EFormField<?> field;
								
	public ValidationEvent(AjaxRequestTarget target, EForm form, EFormField<?> field) {
		super(target);
		this.field = field;
		this.form = form;
	}
	
	public EFormField<?> getField() {
		return this.field;
	}

	public EForm getForm() {
		return form;
	}

	public void setForm(EForm form) {
		this.form = form;
	}
	
}