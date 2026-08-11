package com.novamens.content.base;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum PublishingStatus implements PersistentEnum {
	
	PUBLISHED	(1, "published"), 
	ARCHIVED	(2, "archived"), 
	DELETED		(3, "deleted"),
	DRAFT		(4, "draft"),
	WORKINGCOPY	(5, "workingcopy"),
	LOCKED		(6, "locked"),
	VERSION		(7, "version");
		
	private String label;
	private int id;
	
	private PublishingStatus(int code, String label) {
		this.label = label;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel());
	}

	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PublishingStatus.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}
}