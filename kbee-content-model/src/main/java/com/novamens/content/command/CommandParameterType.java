package com.novamens.content.command;


import com.novamens.security.PersistentEnum;

public enum CommandParameterType implements PersistentEnum {
	
	STRING 	(1,  "String"), 
	DATE (2, "Date"),
	LONG (3, "Long"),
	BOOLEAN (4, "Boolean");
	
	
	
	
	private String label;
	private int id;

	private  CommandParameterType(int id, String label) {this.label = label;this.id = id;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() {return label;}

	public int getId() {return id;}

	//public boolean equals(CommandParameterType o) {return id==o.id;}
	

}
