package com.novamens.content.model;

import java.util.Locale;
import java.util.ResourceBundle;


import com.novamens.security.PersistentEnum;

public enum AttributeType implements PersistentEnum {
	
	DATE    		(2, "Date", "date"), 
	
	NUMBER  		(3, "Integerr", "integer"),
	FLOAT 			(8, "Float", "float"),
	
	STRING  		(1, "String", "string"), 
	TEXT    		(4, "Text", "text"), 
	
	BOOLEAN 		(5, "Boolean", "boolean"),
	
	VALIDITY_FROM 	(6, "Validity From", "validfrom"),
	VALIDITY_TO 	(7, "Validity To", "validto"),
	
	TIMESTAMP    	(8, "Timestamp", "timestamp"),
	
	HTML	    	(9, "HTML", "html");
	
	private String key;
	private String label;
	private int id;
	
	private AttributeType(int code, String label, String key) {
		this.label = label;
		this.id = code;
		this.key=key;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(AttributeType.this.getClass().getName(), locale);
		return res.getString(this.key);
	}
	
	public String getLabel() {
			return getLabel(Locale.getDefault());
	}
	
	public int getId() {
		return id;
	}
}
