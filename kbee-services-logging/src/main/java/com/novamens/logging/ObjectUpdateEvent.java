package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue("ObjectUpdateEvent")
public class ObjectUpdateEvent<T extends com.novamens.dom.Object> extends ObjectEvent<T> {

	public ObjectUpdateEvent() {
		super();
	}

	public ObjectUpdateEvent(T object, String description) {
		super(object, description);
	}

	public ObjectUpdateEvent(T object, List<String> parts)  {
		super(object, parts);
	}
	
	@Override
	public String getAction() {
		return "Update";
	}
}