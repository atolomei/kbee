package com.novamens.content.properties;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.novamens.dom.Domain;

public interface Property {
	
	public PropertyType getType();
	
	public String getName();
	public void setName(String name);
	
	public Object getValue();
	public void setValue(Object value);
	
	/**
	 * Properties can be grouped into Lists (called Set of Properties)
	 * @return
	 */
	public String getSet();
	public void setSet(String set);

	
	public OffsetDateTime getLastModifiedOffsetDateTime();
	public void setLastModifiedOffsetDateTime(OffsetDateTime  date);

	public Serializable getId();

	Domain getDomain();
}
