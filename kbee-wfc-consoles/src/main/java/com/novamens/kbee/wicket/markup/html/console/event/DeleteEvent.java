package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class DeleteEvent<T> extends AbstractWicketAjaxEvent {
	private IModel<T> model;
	
	public DeleteEvent() {
		super(null);
	}
	
	public DeleteEvent(AjaxRequestTarget target, IModel<T> model) {
		super(target);
		setModel(model);
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public T getModelObject() {
		return this.model.getObject();
	}
}
