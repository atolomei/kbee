package com.novamens.dom;

import java.util.List;

public interface Json {
	
	public java.lang.Object get(String name);
				
	public String getString(String name);
	
	
	public List<String> getValues(String name);
	public void put(String name, String value);
	public void remove(String name);
	public void put(String name, List<?> values);
	public boolean isEmpty();
	public String getString(String key, String defaultValue);
	
}
