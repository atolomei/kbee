package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

/**
 *
 * top
 * left 1 .. N
 * 
 * right
 * bottom
 *
 */
public enum PageSectionDisposition implements PersistentEnum {
				
	TOP	 			(0, "top"),
	LEFT			(1, "left"), // default in DB (1 to N PageSection for TABs in main panel
	RIGHT			(2, "right"),
	BOTTOM			(3, "bottom");
	
	private String label;
	private int id;

	private  PageSectionDisposition(int code, String label) {this.label = label;this.id = code;}

	 
	public int getId() {
		return id;
	}
	
	public String getKey() {
		return this.label;
	}

	public String getDisplayName(Locale locale) {
		return getLabel(locale);
	}
	
	public String getDisplayName() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}

	public String toString() {return ("id: " + String.valueOf(getId()) + " | label: "+ getLabel(Locale.getDefault()));}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PageSectionDisposition.this.getClass().getName(), locale);
		return res.getString(this.label);
	}

}
