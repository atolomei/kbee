package com.novamens.kbee.event;

import com.novamens.security.PersistentEnum;

public enum EventType implements PersistentEnum {
	ASSIGN 				(1, "Assign"), 
	UPDATE_CONTENT 		(2, "Update"), 
	ADD_COMMENT_CONTENT (3, "Add comment"),
	ADD_VOTE_CONTENT 	(4, "Add vote");
	
	private String label;
	private int id;
	
	private EventType(int code, String label) {this.label = label;this.id = code;}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() {return label;}
	
	public int getId() {return id;}

}
