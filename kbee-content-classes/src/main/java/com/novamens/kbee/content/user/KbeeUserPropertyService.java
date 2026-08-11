package com.novamens.kbee.content.user;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.properties.Property;
import com.novamens.content.properties.PropertyDao;
import com.novamens.security.User;


public class KbeeUserPropertyService implements UserPropertyService {

	private User user = null;
	
	private PropertyDao dao = null;

	public KbeeUserPropertyService() {
	}
	
	public KbeeUserPropertyService(User user) {
		 this.user= user;
	}
	
	@Override
	public Object getProperty(String name) {
		Object value = null;
		for (Property property : getProperties()) {
			if (property.getName().equals(name)) {
				value = property.getValue();
				break;
			}
		}
		return value;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeProperty(Property property) {
		getPropertyDao().delete(property);
	}

		
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
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
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setProperty(String name, Object value) {
		Property property = null;
		for (Property p : getProperties()) {
			if (p.getName().equals(name)) {
				property = p;
				break;
			}
		}
		
		if (property == null) {
			property = new KbeeUserProperty();
			property.setName(name);
			((KbeeUserProperty) property).setUser(getUser());
		}
		
		property.setValue(value);
		getPropertyDao().save(property);
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setProperty(String name, String set, Object value) {
		
		Property property = null;
		for (Property p : getPropertiesSet(set)) {
			if (p.getName().equals(name)) {
				property = p;
				break;
			}
		}
		
		if (property == null) {
			property = new KbeeUserProperty();
			property.setName(name);
			property.setSet(set);
			property.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			((KbeeUserProperty) property).setUser(getUser());
		}
		
		property.setValue(value);
		getPropertyDao().save(property);
	}
	
	
			
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeProperty(String name, String set) {
		Property property = null;
		for (Property p : getPropertiesSet(set)) {
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


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void removePropertiesSet(String set)	{
		for (Property p : getPropertiesSet(set)) {
			getPropertyDao().delete(p);	
		}
	}
	
	
	@Override
	public List<Property> getPropertiesSet(String set) {
		return getPropertyDao().findPropertiesByUser(getUser(), set);
	}

	@Override
	public List<Property> getPropertiesSet(String set, int maxItems) {
		return getPropertyDao().findPropertiesByUser(getUser(), set, maxItems);
	}
	

	
	public List<Property> getProperties() {
		return getPropertyDao().findPropertiesByUser(getUser());
	}

	
	public PropertyDao getPropertyDao() {
		return this.dao;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setPropertyDao(PropertyDao dao) {
		this.dao = dao;
	}

}
