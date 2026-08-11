package com.novamens.content.form;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.novamens.content.model.Classificable;
import com.novamens.event.Event;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "type")
public interface EFieldModel<T> extends Serializable {
	// Update object with data
	public void set(Object object, List<T> data);
	public void set(Object object, Object data);
	// get data from object
	public T get(Object object);
	public List<T> getValues(Object object);
	// can handle event
	public boolean handle(Event event);
	// handle event. Return the list of resultant values or null
	public List<T> onEvent(Event event);
	// data source defined by object as a context
	public EFormDataSource<T> getDataSource(Classificable object);
	// Serialization
	public String serialize(Classificable formobject, T object);
	public T deserialize(Classificable formobject, String token);
	// Relations, parents, etc
	public String getMetainfoMessage();
	// Read only model
	public boolean isReadOnly();
	 	
	// Specification error message: validate the form and object applicability
	public String getErrorMessage(Object object);
	
	public String getTypeLabel();
	
	public String getDescription(Locale locale);
	@JsonIgnore 
	default public String getDescription() {return getDescription(Locale.getDefault());}
	
	
	
	public String getModelObjectName(Locale locale);
	@JsonIgnore
	default public String getModelObjectName() {return getModelObjectName(Locale.getDefault());}
	
	
	
} 