package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ObjectDeleteEvent")
public class ObjectDeleteEvent<T extends com.novamens.dom.Object> extends ObjectEvent<T> {

	public ObjectDeleteEvent() {
		super();
	}
	
	public ObjectDeleteEvent(T object, String description) {
		super(object, description);
	}
	
	public ObjectDeleteEvent(T object, List<String> updatedParts) {
		super(object, updatedParts);
	}
	
	@Override
	public String getAction() {
		return "Delete";
	}
}
