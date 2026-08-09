package com.novamens.workflow;

public enum ResolutionAction {
	
	SETNULL (1, "Set Null"), 
	TRANSFER (2, "Transfer"); 
	
	private String label;
	private int id;
	
	private ResolutionAction(int code, String label) {
		this.label = label;
		this.id = code;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	public String getLabel() {
		return label;
	}
	
	public int getId() {
		return id;
	}
}