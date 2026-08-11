package com.novamens.portal.model.block;

import com.novamens.security.PersistentEnum;

public enum BlockType implements PersistentEnum {

	LIST_VIEW_SITE 			(1, "List View Site"), 
	LIST_SITE 				(2, "List Site"),
	LIST_VIEW_CONTENT		(3, "List View Content"), 
	LIST_CONTENT 			(4, "List Content"),
	FEATURED_CONTENT		(5, "Destacados"), 
	DR_BIT 					(6, "Dr. Bit"),
	HOLA					(7, "Hola!"),
	XBLOCK					(8, "XBlock");

	private String label;
	private int id;

	private  BlockType(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() {return label;}

	public int getId() {return id;}
	
}
