package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormEvent;
import com.novamens.content.form.EFormField;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class EAjaxFormEvent extends AbstractWicketAjaxEvent implements EFormEvent  {
	
	EFormField<?> field = null;
	EFormData data = null;
	
	public EAjaxFormEvent(AjaxRequestTarget target) {
		super(target);
	}
	
	public EAjaxFormEvent(AjaxRequestTarget target, EFormField<?> field) {
		super(target);
		this.field = field;
	}
	
	public EAjaxFormEvent(AjaxRequestTarget target, EFormField<?> field, EFormData data) {
		super(target);
		this.field = field;
		this.data = data;
	}
	
	@Override
	public Object getObject() {
		return getField();
	}
	
	public EFormField<?> getField() {
		return field;
	}
	
	public EFormData getFormData() {
		return data;
	}  
}