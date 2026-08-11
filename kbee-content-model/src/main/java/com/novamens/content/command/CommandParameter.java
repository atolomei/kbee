package com.novamens.content.command;

import java.io.Serializable;



public class CommandParameter implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	String description;
	boolean isOptional;
	CommandParameterType type;
	
	
	public CommandParameter(String name, String description, boolean isOptional, CommandParameterType type) {
		super();
		this.name = name;
		this.description = description;
		this.isOptional = isOptional;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isOptional() {
		return isOptional;
	}
	
	public CommandParameterType getType() {
		return type;
	}
	
	
	
}
