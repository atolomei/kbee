package com.novamens.content.user;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum UserProfileType implements PersistentEnum {
 
	WORKFLOW_PARTICIPANT			(1, "workflow"), 	
	EMPLOYEE						(2, "employee"),
	CLIENT							(3, "client"),
	READONLY						(4, "readonly");
		
	private int id;
	private String label;
		
	private  UserProfileType(int code, String label) {
		this.label = label;
		this.id = code; 
	}
		
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
		
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
		
	public String getLabel(Locale locale) {			
		ResourceBundle res = ResourceBundle
			.getBundle(UserProfileType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}
}