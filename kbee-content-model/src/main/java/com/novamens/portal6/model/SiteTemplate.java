package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum SiteTemplate implements PersistentEnum {  
	
	INTEREST_GROUP 			(1, "interest-group"), 
	ORGANIZATIONAL_AREA 	(3, "area"),
	STANDALONE				(5, "standalone"),  // Application. Digital Library. Dr bit. 
	SEARCHER				(6, "general"),  // Owner portal,
	DR_BIT					(7, "drbit"),
	HOME					(8, "home"),
	DIRECTORY				(9, "directory"),
	KNOWLEDGE_BASE			(10, "kbase"),
	DEAL_ROOM				(11, "dealroom"),
	EXTERNAL				(99, "external");

	private String label;
	private int id;

	private SiteTemplate(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel(Locale.getDefault()));} 
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}

	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(SiteTemplate.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {return id;}
	
	public String getKey() {return label;}
	
}
