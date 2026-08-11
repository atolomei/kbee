package com.novamens.kbee.content.properties;

import java.util.List;

import com.novamens.content.properties.ObjectPropertyService;
import com.novamens.content.properties.Property;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

public class KbeeObjectPropertyService implements ObjectPropertyService  {
	
	private com.novamens.dom.Object object = null;
	private PropertyDao dao = null;

	public KbeeObjectPropertyService() {
	}
	
	public KbeeObjectPropertyService(com.novamens.dom.Object object) {
		 this.object = object;
	}
	
	public Object getProperty(String name) {
		Object value = null;
		for (Property property : getProperties()) {
			if (property.getName().equals(name)) {
				value = property.getValue();
				break;
			}
		}
		return value;
	};
	
	public void removeProperty(String name) {
		Property property = null;
		for (Property p : getProperties()) {
			if (p.getName().equals(name)) {
				property = p;
				break;
			}
		}
		
		if (property == null) {
			return;
		}
		
		getPropertyDao().delete(property);
	}
	
	public void setProperty(String name, Object value) {
		Property property = null;
		for (Property p : getProperties()) {
			if (p.getName().equals(name)) {
				property = p;
				break;
			}
		}
		
		if (property == null) {
			property = new KbeeObjectProperty();
			property.setName(name);
			((KbeeObjectProperty) property).setObject(getObject());
			((KbeeObjectProperty) property).setDomain(getDomain());
		}
		
		
		property.setValue(value);
		
		getPropertyDao().save(property);
	}
	
	public com.novamens.dom.Object getObject() {
		return object;
	}
	
	public List<Property> getProperties() {
		return getPropertyDao().findPropertiesByObject(getObject());
	}
	
	public PropertyDao getPropertyDao() {
		return this.dao;
	}
	
	public void setPropertyDao(PropertyDao dao) {
		this.dao = dao;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
