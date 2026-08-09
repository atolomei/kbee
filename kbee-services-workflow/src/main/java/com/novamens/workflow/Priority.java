package com.novamens.workflow;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;



public enum Priority implements Serializable  {
	
	Low 			(2, "Low", "priority-low" ), 
	Standard 		(4, "Normal", "priority-standard"), 
	High 			(6, "High", "priority-high" ),
	Urgent 			(8, "Urgent", "priority-urgent" );
	
	private String label;
	private int id;
	private String css;
	
	private  Priority(int code, String label, String css) {
		this.label = label;
		this.id = code;
		this.css=css;
	}

	
	public String toString() {
		return ("id: " + String.valueOf(getId()) + " | label: "+ getLabel() + " | css: " + getCss());
	}

	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(Priority.this.getClass().getName(), locale);
		return res.getString(this.label);
	}

	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public int getId() {
		return id;
	}
	
	public String getCss() {
		return css;
	}
}
