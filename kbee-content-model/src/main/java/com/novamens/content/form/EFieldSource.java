package com.novamens.content.form;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;


public enum EFieldSource implements PersistentEnum {
	UserInput (1, "UserInput"), 
	Script (2, "Script");
	
	private String label;
	private int id;
	
	private  EFieldSource(int code, String label) {
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
	
	public String getLabel(Locale locale) {
		ResourceBundle rb = ResourceBundle.getBundle(EFieldSource.this.getClass().getName(), locale);
		String label = rb.getString(this.label);
		return label;
	}
}
