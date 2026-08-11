package com.novamens.kbee.content.user;

import java.util.List;

import com.novamens.content.properties.Property;
import com.novamens.service.ObjectService;

/**
 * User Properties can be grouped into "Sets".
 * A set of properties is a list, sorted by date modified desc.
 * 
 * The name of the properties in set must be different.
 * 
 *  We use a set of properties to save the history of
 *  SQL Queries executed by the user in the SQL Gateway Page.
 *  
 *  query-0, sql-history, select ...
 *  query-1, sql-history, select ...
 *  query-2, sql-history, select ...
 *
 */
public interface UserPropertyService extends ObjectService {

	public Object getProperty(String name);
	public void removeProperty(String name);
	public void setProperty(String name, Object value);

	/**
	 * Sets
	 */
	public void setProperty(String name, String set, Object value);
	public void removeProperty(String name, String set);
	public void removePropertiesSet(String set);
	public List<Property> getPropertiesSet(String set);
	
	public void removeProperty(Property property);
	public List<Property> getPropertiesSet(String key, int maxItems);
	 
}
