package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;
	
/**
 * Types of sites managed by kbee portal, including external sites.
 * @see KbeeSite
 * 
 */
public enum SiteType implements PersistentEnum {


	// 
	INSTITUTIONAL 			(90, "institutional"),
	MINISITE	 			(91, "minisite"),
	
	//
	GENERAL	 				(1, "general"),
	HOME					(10, "home"),
	
	
	// tools
	KNOWLEDGE_BASE			(20, "kbase"),
	DEAL_ROOM				(30, "dealroom"),
	
	LIBRARY					(40, "library"),
	PROJECT					(41, "project"),
	
	
	GENERAL_DASHBOARD	 	(41, "general_dasboard"),  	
	SIMPLE_SUBMIT_SITE		(50, "simple-submit"),
	TUTORIAL				(60, "tutorial"),
	HELP					(70, "help"),
	
	INTERNAl_WEBAPP			(80, "internal"),
	EXTERNAL				(999, "external");
	
	private String label;
	private int id;

	private  SiteType(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel(Locale.getDefault()));} 
	
	
	public String getDisplayName(Locale locale) {
		return getLabel(locale);
	}
	
	public String getDisplayName() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}

	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(SiteType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	public int getId() {return id;}
	
	
	public String getKey() {
		return this.label;
	}
}

