package com.novamens.content.command;

import com.novamens.security.PersistentEnum;

public enum CommandState implements PersistentEnum {
	NOT_STARTED (1, "NOT STARTED", 			"notstarted"), 
	RUNNING 	(2, "RUNNING", 				"running"), 
	COMPLETED 	(3, "COMPLETED", 			"done"),
	CANCELED 	(4, "CANCELLED", 			"cancelled"),
	PAUSED		(5, "PAUSED", 				"paused"),
	ERROR		(6, "ERROR", 				"error"),
	UNKNOWN		(7, "UNKNOWN", 				"unknown");
	
	private String label;
	private int id;
	private String css;
	
	private  CommandState(int code, String label, String css) 	{this.label = label;this.id = code; this.css=css;}
	public String toString() 									{return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() 									{return label;}
	public String getCss() 									{return css;}
	public int getId() 											{return id;}
	
}
