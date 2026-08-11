package com.novamens.content.form;

import com.novamens.security.PersistentEnum;

public enum EDisposition implements PersistentEnum {
	VERTICAL 	(1, "vertical"), 
	HORIZONTAL 	(2, "horizontal");
	
	private String label;
	private int id;
	
	private EDisposition(int id, String label) {
		this.label = label;
		this.id = id; 
	}
	
	public int getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
}
