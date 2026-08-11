package com.novamens.content.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum Multiplicity implements PersistentEnum {
	M01 (1, "M01", false), 
	M11 (2, "M11", false), 
	M1N (3, "M1N", true),
	M0N (4, "M0N", true);
	
	private String label;
	private int id;
	private boolean multiple;
	
	private  Multiplicity(int code, String label, boolean multiple) {
		this.label = label;
		this.id = code;
		this.multiple = multiple;
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
	
	public boolean isMultiple() {
		return multiple;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle rb = ResourceBundle.getBundle(Multiplicity.this.getClass().getName(), locale);
		String label = rb.getString(this.label);
		return label;
	}
}