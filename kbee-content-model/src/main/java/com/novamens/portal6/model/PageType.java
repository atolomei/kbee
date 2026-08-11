package com.novamens.portal6.model;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;


/**
 * . Standard: Páginas agregadoras
 * .link: Referencia contenido o url externa
 * 
 */
public enum PageType implements PersistentEnum {
	
	STANDARD 			(0, "standard"), 
	LINK				(1, "link"); 
		
	private String label;
	private int id;
											
	private PageType(int code, String label) {this.label = label;this.id = code;}

	public String toString()	{return ("id: " + getId() + ". label: "+ getLabel());} 
	public String getLabel() 	{return getLabel(Locale.getDefault());}
	public int getId() 			{return id;}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PageType.this.getClass().getName(), locale);
		return res.getString(this.label);
	}

	
	

}
