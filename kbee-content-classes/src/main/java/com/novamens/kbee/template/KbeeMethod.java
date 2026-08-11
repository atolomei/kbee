package com.novamens.kbee.template;

import java.lang.reflect.InvocationTargetException;

import com.novamens.util.JXPath;

public class KbeeMethod {
	
	private String name;
	private String path;
	
	public KbeeMethod(String name) {
		this.name = name;
	}
	
	public KbeeMethod(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	public String getName() {
		return name;
	}
	
	public String path() {
		return path;
	}
	
	public Object evaluate(Object object) {
		Object value = null;
		try {
			JXPath path = new JXPath(path());
			value = object!=null ? path.evaluate(object) : null;
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
		}
		return value;
	}
}