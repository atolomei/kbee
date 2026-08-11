package com.novamens.content.properties;

import com.novamens.service.ObjectService;

public interface PropertyService extends ObjectService {
	public Object getProperty(String name);
	public Object reloadProperty(String name);
	public void removeProperty(String name);
	public void updateProperty(String name, Object value);
	public void setProperty(String name, Object value);
	static public String PROPERTY_HAS_TAGS ="tags";
}
