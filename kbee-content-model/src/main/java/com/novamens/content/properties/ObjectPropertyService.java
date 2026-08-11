package com.novamens.content.properties;

import com.novamens.service.ObjectService;

public interface ObjectPropertyService extends ObjectService {
	public Object getProperty(String name);
	public void removeProperty(String name);
	public void setProperty(String name, Object value);
}
