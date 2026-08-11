package com.novamens.kbee.content.properties;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.properties.Property;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.properties.PropertyService;


public class KbeePropertyService implements PropertyService {
	
	private Content content = null;
	private PropertyDao dao = null;

	public KbeePropertyService() {
	}
	
	public KbeePropertyService(Content content) {
		 this.content = content;
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
	
	public Object reloadProperty(String name) {
		Object value = null;
		for (Property property : getProperties()) {
			if (property.getName().equals(name)) {
				property = getPropertyDao().reload(property);
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
	
	@Transactional
	public void updateProperty(String name, Object value) {
		setProperty(name, value);
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
			property = new KbeeProperty();
			property.setName(name);
			((KbeeProperty) property).setContent(getContent());
		}
		
		property.setValue(value);
		
		getPropertyDao().save(property);
	}
	
	public Content getContent() {
		return content;
	}
	
	public List<Property> getProperties() {
		return getPropertyDao().findPropertiesByContent(getContent());
	}
	
	public PropertyDao getPropertyDao() {
		return this.dao;
	}
	
	public void setPropertyDao(PropertyDao dao) {
		this.dao = dao;
	}
}
