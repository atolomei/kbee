package com.novamens.kbee.wicket.markup.html.console.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ClickEvent<T> extends AbstractWicketAjaxEvent {
	
	private IModel<T> model;
	private int index;
	private String context;
	private boolean is_new_tab = true;
	
	public String getContext() {
		return context;
	}
	
	public boolean isNewTab() {
		return ( is_new_tab);
	}
	public ClickEvent() {
		super(null);
	}
	
	public ClickEvent(AjaxRequestTarget target, IModel<T> model, int index) {
			this(target, model, index, null);
	}
	
	public ClickEvent(AjaxRequestTarget target, IModel<T> model, int index, String context) {
		super(target);
		this.context=context;
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
