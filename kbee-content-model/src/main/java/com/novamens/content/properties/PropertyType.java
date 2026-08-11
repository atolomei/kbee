package com.novamens.content.properties;

import com.novamens.security.PersistentEnum;

public enum PropertyType implements PersistentEnum {
	STRING (1, "STRING"), 
	LONG (2, "LONG"), 
	DELETED (3, "DATE");
	
	private String label;
	private int id;
	
	private PropertyType(int code, String label) {
		this.label = label;
		this.id = code;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	public String getLabel() {
		return label;
	}
	
	public int getId() {
		return id;
	}
}
