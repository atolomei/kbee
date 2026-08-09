package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class SingleSelectionEvent<T> extends AbstractWicketAjaxEvent {
	private IModel<T> model;
	private int index;

	public SingleSelectionEvent() {
		super(null);
	}
	
	public SingleSelectionEvent(AjaxRequestTarget target, IModel<T> model, int index) {
		super(target);
		setModel(model);
		setIndex(index);
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	public void setIndex(int i) {
		index=i;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public T getModelObject() {
		return this.model.getObject();
	}

}
