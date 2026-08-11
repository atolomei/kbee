package com.novamens.kbee.content.text.template;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TemplateData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> values = new HashMap<String, String>();
	
	public String get(String key) {
		return values.get(key);
	}
	
	public void put(String key, String value) {
		values.put(key, value);
	}
	
	public Map<String, String> getValues() {
		return values;
	}
	
	public void setValues(Map<String, String> values) {
		this.values = values;
	}
}
