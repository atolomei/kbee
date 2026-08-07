package com.novamens.security;

import java.util.Locale;
import java.util.ResourceBundle;



public enum ReservedUsername {

	ROOT 					(1, "root", 			"root", 			"fa-check-circle"), 
	WORKFLOW 				(2, "workflow", 		"workflow", 		"fal fa-archive"),
	PENDING 				(2, "workflow", 		"workflow", 		"fal fa-archive"),
	PORTAL 					(4, "portal", 			"portal", 		    "fal fa-archive"),
	PUBLICRESOURCES 		(3, "publicresources", 	"publicresources", 	"fal fa-minus-circle");
		
	private String label;
	private int id;
	private String css;
	private String icon;
	
	
	
	static public boolean isReserved( String username) {
		
		if (username==null)
			return true;
		
		String a_prefix[] = username.split("@");
		String prefix;
		
		if (a_prefix.length<1)
			prefix=username;
		else
			prefix=a_prefix[0];
		
			return  prefix.toLowerCase().equals(ROOT.label) ||
					prefix.toLowerCase().equals(WORKFLOW.label) ||
					prefix.toLowerCase().equals(PENDING.label) ||
					prefix.toLowerCase().equals(PORTAL.label) ||
					prefix.toLowerCase().equals(PUBLICRESOURCES.label); 
				

	}
	
	private ReservedUsername(int code, String label, String css, String icon) {
		this.label = label;
		this.id = code; 
		
		this.css=css;
		this.icon=icon;
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();
	}
	
	public String getUserName() {
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
		ResourceBundle res = ResourceBundle.getBundle(ReservedUsername.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ReservedUsername.this.getClass().getName(), locale);
		return  "<span class=\"" + getCss() + "\">" + res.getString(this.label) + "</span>";
	}
	
}
