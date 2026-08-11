package com.novamens.content.base;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum ResourceGroupType implements PersistentEnum, Serializable {
	DEFAULT	(1, "default_group", "default_group"),
	REVISIONS (2, "revisions_group", "revisions_group"),
	MANAGEMENT (3, "management_group", "management_group_group");
		
	private String label;
	private int id;
	private String css;
	
	private ResourceGroupType(int code, String label, String css) {
		this.label = label;
		this.id = code; 
		this.css=css;
	}
	
	public String toString()	{
		return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();
	}
	
	public String getLabel() 	{
		return getLabel(Locale.getDefault());
	}
	
	public String getCss() {
		return css;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ResourceGroupType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId()	{
		return id;
	}
	
	public String getDisplayName() {
		return getLabel();
	}
}