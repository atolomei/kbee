package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;


/**
 * Depending on the Area 
 * it can be Left, Center or Right
 */
public enum AreaSection implements PersistentEnum {

	LEFT 	(1, 	"left", 	10), 
	CENTER 	(2, 	"center", 	20),
	RIGHT 	(3, 	"right", 	30),
	
	INTERNAL_MULTI_BLOCK 			(999, "internal-multi-block", 999);
	
	private String label;
	private int id;
	private int sort_order;
	
	private AreaSection(int code, String label, int sort_order) {this.label = label;this.id = code; this.sort_order=sort_order;}
	
	public String toString()	{return ("id: " + String.valueOf(getId()) + "  label: "+ getLabel() + "  sort: " + String.valueOf(sort_order));} 
	public String getLabel() 	{return getLabel(Locale.getDefault());}
	public int getSortOrder() 	{return this.sort_order;	}
	public int getId() 			{return id;}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(AreaSection.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	
}
