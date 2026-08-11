package com.novamens.content.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;


public enum DataSetType implements PersistentEnum {
	
	STRING 						(1, "string"), 
	
	
	
	// This type is deprecated, do not use  (it was transformed into Attribute
	//
	DATE 						(2, "date"),         // Deprecated
	
	
	
	PEOPLE						(3, "person"),           
	USER						(4, "user"),			// System Dataset. Maps Users
	USERSUBSET					(5, "usersubset"),	 	// User Defined subset of the Users Set
	EXTERNAL					(6, "external"),		
	SECURED 					(7, "secured"),			// Deprecated
	SIGNER						(8, "organizational"),  // Deprecated
	BOOLEAN						(9, "boolean"),         // deprecated
	LABEL						(10, "label"),
	ENTITY						(11, "entity");
	
	private String label;
	private int id;

	private DataSetType(int code, String label) {
		this.label = label;
		this.id = code;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(DataSetType.class.getName(), locale);
		return res.getString(this.label);
	}
	
	public String getLabel() {
		ResourceBundle res = ResourceBundle.getBundle(DataSetType.class.getName(), Locale.getDefault());
		return res.getString(this.label);
	}

	public int getId() {
		return id;
	}
}
