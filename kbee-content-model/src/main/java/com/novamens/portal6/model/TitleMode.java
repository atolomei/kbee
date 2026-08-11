package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum TitleMode implements PersistentEnum {
	
	SIMPLE	 		(1, "simple", 			"simple"), 
	PHOTORIGHT 		(2, "photo-right", 		"photoright"), 
	PHOTOLEFT 		(3, "photo-left", 		"photoleft"),
	PHOTOOVERLAY	(4, "photo-overlay", 	"photooverlay"),
	PHOTOTOP		(5, "phototop", 		"phototop");
		
	private String label;
	private int id;
	private String css;
	
	private TitleMode(int code, String label, String css) {
		this.label = label;
		this.id = code; 
		this.css=css;
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();
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
		ResourceBundle res = ResourceBundle.getBundle(TitleMode.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(TitleMode.this.getClass().getName(), locale);
		return  "<span class=\"" + getCss() + "\">" + res.getString(this.label) + "</span>";
	}

}
