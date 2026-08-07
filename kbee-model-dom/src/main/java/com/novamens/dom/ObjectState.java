package com.novamens.dom;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum ObjectState implements PersistentEnum {
	
	ENABLED 	(1, "enabled", "enabled", "fa-check-circle"), 
	ARCHIVED 	(2, "archived", "archived", "fal fa-archive"), 
	DELETED 	(3, "deleted", "deleted", "fal fa-minus-circle"),
	DRAFT 		(4, "draft", "draft", "fal fa-pen-square");
		
	private String label;
	private int id;
	private String css;
	private String icon;
	
	private ObjectState(int code, String label, String css, String icon) {
		this.label = label;
		this.id = code; 
		
		this.css=css;
		this.icon=icon;
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();
	}
	
	public String getCode() {
		return label;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getCss()	{
		return css;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ObjectState.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ObjectState.this.getClass().getName(), locale);
		return  "<span class=\"" + getCss() + "\">" + res.getString(this.label) + "</span>";
	}
}