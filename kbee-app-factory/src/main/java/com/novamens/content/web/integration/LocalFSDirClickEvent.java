package com.novamens.content.web.integration;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class LocalFSDirClickEvent<T> extends AbstractWicketAjaxEvent {
				
	LocalFSQuery query;

	private IModel<T> model;
	private int index;
	
	
	public LocalFSDirClickEvent() {
		super(null);
	}
	
	public LocalFSDirClickEvent(AjaxRequestTarget target, LocalFSQuery query, IModel<T> model, int index) {
		super(target);
		this.query=query;
		setModel(model);
		this.index = index;
	}
	
	public LocalFSQuery getQuery() {
		return this.query;
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
