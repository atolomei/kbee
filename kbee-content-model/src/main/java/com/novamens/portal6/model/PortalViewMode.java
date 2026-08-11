package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;



/**
 * 
 * Edit -> controller and payload invisible
 * production -> 
 * preview -> payload visible
 * 
 * 
 *
 */
public enum PortalViewMode {

	PRODUCTION 		(1, "production", 			"production", 		"fa-check-circle"), 
	EDIT 			(2, "edit", 				"edit", 			"fal fa-archive"),
	PREVIEW 		(3, "preview", 				"preview", 		"fa-check-circle");
	
	//DEBUG		 	(3, "debug_visible", 		"debug_visible", 	"fal fa-minus-circle");
		
	private String label;
	private int id;
	private String css;
	private String icon;
	
	/**
	 * 
	 * Site 			*****
	 * Page 			****
	 * PageSection 		***
	 * Area 			**
	 * Block 			*
	 * 
	 * 
	 * @param code
	 * @param label
	 * @param css
	 * @param icon
	 */
	private PortalViewMode(int code, String label, String css, String icon) {
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
		ResourceBundle res = ResourceBundle.getBundle(PortalViewMode.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PortalViewMode.this.getClass().getName(), locale);
		return  "<span class=\"" + getCss() + "\">" + res.getString(this.label) + "</span>";
	}
}
