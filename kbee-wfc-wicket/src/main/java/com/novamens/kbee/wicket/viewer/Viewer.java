package com.novamens.kbee.wicket.viewer;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public interface Viewer<T> extends Serializable {
	
	public IModel<T> getModel();
	public T getModelObject();
}
