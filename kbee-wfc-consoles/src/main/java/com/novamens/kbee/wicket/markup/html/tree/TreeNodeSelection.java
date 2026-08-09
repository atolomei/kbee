package com.novamens.kbee.wicket.markup.html.tree;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class TreeNodeSelection<T> extends AbstractWicketAjaxEvent  {
	 
	IModel<T> model;
	
	public TreeNodeSelection(AjaxRequestTarget target, IModel<T> model) {
		super(target);
		this.model = model;
	}
	
	@Override
	public Object getObject() {
		return getNode();
	}
	
	public T getNode() {
		return getModel()!=null ? getModel().getObject() : null;
	}
	
	public IModel<T> getModel() {
		return model;
	}
}