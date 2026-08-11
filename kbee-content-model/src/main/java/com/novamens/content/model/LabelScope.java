package com.novamens.content.model;

import com.novamens.security.PersistentEnum;

public enum LabelScope implements PersistentEnum {
	User (1, "User"), 
	Application (2, "Application"), 
	Workflow (3, "Workflow");
	
	private String label;
	private int id;
	
	private  LabelScope(int code, String label) {
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
