package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.form.EFormField;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EFocusEvent extends AbstractWicketAjaxEvent  {
	
	EFormField<?> field = null;
	
	public EFocusEvent(AjaxRequestTarget target) {
		super(target);
	}
	
	public EFocusEvent(AjaxRequestTarget target, EFormField<?> field) {
		super(target);
		this.field = field;
	}
	
	public EFormField<?> getField() {
		return field;
	}
}