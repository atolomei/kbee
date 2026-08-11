package com.novamens.content.form;

public enum EComponentType {
	
	BOOLEAN ("Boolean"),
	COMBO ("Combo"),
	DATE ("Date"),
	HTML ("Html"),
	LIST ("List"),
	NUMBER ("Number"),
	RESOURCE_SYSTEM("Resource System"),
	RESOURCES("Resources"),
	RESOURCE("Resource"),
	ROW ("Row"),
	SECTION ("Section"),
	STATIC_TEXT ("Static Text"),
	STRING ("String"),
	TEXT ("Text"),
	TITLE ("Title");
	
	private String label;
	
	private EComponentType(String label) {
		this.label = label;
	}
	
	public String toString() {
		return (getLabel());
	}
	
	public String getLabel() {
		return label;
	}
}