package com.novamens.wicket.model;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

public class SerializableModel<T> implements IModel<T> {
	private static final long serialVersionUID = 1L;
	private T object;
	
	public SerializableModel() {
	}
	
	public SerializableModel(T object) {
		this.object = object;
	}
	
	public T getObject() {
		return object;
	}
	
	public void setObject(T object) {
		this.object = object;
	}
	
	public void detach() {
		if (object instanceof IDetachable) {
			((IDetachable)object).detach();
		}
	}
}