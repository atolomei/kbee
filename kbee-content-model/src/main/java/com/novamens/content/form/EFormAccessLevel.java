package com.novamens.content.form;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum EFormAccessLevel implements PersistentEnum {
	
	
	GENERAL_LIBRARY 	(5, "general-library"), 	// only libraries
	GENERAL_PORTAL 		(1, "general-portal"), 		// only portal
	WORKFLOW			(2, "workflow"), 			// only tasks

	GENERAL 			(4, "general"), 			// tasks and libraries and portals
	INTERNAL_INFO		(6, "internal"), 			// internal info
	PROCESS_LAUNCHER	(8, "process-launcher"), 	
	
	@Deprecated
	FILE_CONTAINER_DEPRECATED		(7, "file-container"),
	
	@Deprecated
	INLINE_DEPRECATED				(3, "inline"); // only inside expanded hit panels
		
	private String label;
	private int id;
	
	private EFormAccessLevel(int code, String label) {
		this.label = label;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel());
	}
	
	public String getCode() {
		return label;
	}
	
	public String getDisplayName() {
		return getLabel();
	}
	
	public String getKey() {
		return this.label;
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		try {
			ResourceBundle res = ResourceBundle.getBundle(EFormAccessLevel.this.getClass().getName(), locale);
			return res.getString(this.label);
		} 
		catch (Exception e)  {
			//logger.error(e);
			return this.label +" (not found)";
		}
	}
	
	public int getId() {
		return id;
	}
}