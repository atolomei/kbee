package com.novamens.kbee.content.form;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EPropertyModel;
import com.novamens.content.model.Classificable;
import com.novamens.event.Event;

public abstract class  KbeeEPropertyFieldModel<T> implements EFieldModel<T> {
	private static final long serialVersionUID = 1L;
	
	private String property;
	
	public String getProperty() {
		return this.property;
	}
	
	public void setProperty(String attributeId) {
		this.property = attributeId;
	}
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public boolean handle(Event event) {
		return false;
	}
	
	@Override
	public List<T> onEvent(Event event) {
		return new ArrayList<T>();
	}
	
	@Override
	@JsonIgnore
	public String getMetainfoMessage() {
		return null;
	}
	
	@Override
	public EFormDataSource<T> getDataSource(Classificable object) {
		return null;
	}
	
	@Override
	public void set(Object object, List<T> data) {
		Method method = ReflectionUtils.findMethod(object.getClass(), getProperty(), data.getClass());
		if (method != null) {
			ReflectionUtils.invokeMethod(method, object, data);
		} 
	}
	
	@Override
	public void set(Object object, Object data) {
		Method method = getSetter(object);
		if (method != null) {
			ReflectionUtils.invokeMethod(method, object, data);
		} 
	}
	
	// get data from object
	@Override
	public T get(Object object) {
		Method method = getGetter(object);
		if (method != null) {
			@SuppressWarnings("unchecked")
			T value = (T) ReflectionUtils.invokeMethod(method, object);
			return value;
		} 
		return null;
	}
	
	public List<T> getValues(Object object) {
		List<T> values = new ArrayList<T>();
//		for (String stringvalue : ((Classificable)object).getAttributeValues(getAttribute())) {
//			values.add(getValueOf(stringvalue));
//		}
		return values;
	}
	
	@Override
	public String serialize(Classificable formobject, T value) {
		return value!=null ? value.toString() : null;
	}
	
	@Override
	public T deserialize(Classificable formobject, String token) {
		return token!=null ? getValueOf(token) : null;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		Method method = ReflectionUtils.findMethod(object.getClass(), "set"+getProperty(), getTypeOfT());
		if (method==null) {
			String message = "Property ";
			message += getProperty();
			message += " not found";
			return message;
		}
		method = ReflectionUtils.findMethod(object.getClass(), "get"+getProperty());
		if (method==null) {
			String message = "Property ";
			message += getProperty();
			message += " not found";
			return message;
		}
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		String description = getModelObjectName(locale) + " " + getProperty();
		return description;
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return "Property";
	//	//ResourceBundle res = ResourceBundle.getBundle( KbeeEAttributeFieldModel.class.getName(), Locale.getDefault());
	//	//return res.getString("attribute");
	}
	
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EPropertyModel.GetTypeLabel();
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getTypeOfT() {
		Class<T> typeOfT = (Class<T>)
				((ParameterizedType)getClass()
				.getGenericSuperclass())
				.getActualTypeArguments()[0];
		return typeOfT;
	}
	
	
	protected Method getGetter(Object object) {
		String property = getProperty();
		property = property.substring(0,1).toUpperCase() + property.substring(1);
		Method method = ReflectionUtils.findMethod(object.getClass(), "get"+property);
		return method;
	}
	
	protected Method getSetter(Object object) {
		String property = getProperty();
		property = property.substring(0,1).toUpperCase() + property.substring(1);
		Method method = ReflectionUtils.findMethod(object.getClass(), "set"+property, String.class);
		return method;
	}

	protected abstract T getValueOf(String value);
}