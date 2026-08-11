package com.novamens.content.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum AccessStrategy implements PersistentEnum {
	Roles (1, "Roles"), 
	All (2, "All"), 
	Iql (3, "Iql"),
	Script (4, "Script"),
	Managed (5, "Managed"),
	Readables(6, "Readables"),
	Writeables(7, "Writeables"),
	ChildsEnabled(8, "ChildsEnabled");
	
	private String label;
	private int id;
	
	private  AccessStrategy(int code, String label) {
		this.label = label;
		this.id = code;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return label;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle rb = ResourceBundle.getBundle(AccessStrategy.this.getClass().getName(), locale);
		String label = rb.getString(this.label);
		return label;
	}
}
