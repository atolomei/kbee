package kbee.web.eform;

import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormComponent;

public class ComponentModel<T extends EFormComponent> implements IModel<T> {
	private static final long serialVersionUID = 1L;
	
	T field;
	
	public ComponentModel(T field) {
		this.field = field;
	}
	
	public T getObject() {
		return field;
	}
	
	public void setObject(T field) {
		this.field = field;
	}
	
	public void detch() {
	}
}