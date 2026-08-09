package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class DoubleClickEvent<T> extends AbstractWicketAjaxEvent {
	private IModel<T> model;
	private int index;
	
	public DoubleClickEvent() {
		super(null);
	}
	
	public DoubleClickEvent(AjaxRequestTarget target, IModel<T> model, int index) {
		super(target);
		setModel(model);
		this.index = index;
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
	
	public int getIndex() {
		return this.index;
	}
}
