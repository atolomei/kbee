package com.novamens.workflow;

public enum DueDateAction {
	
	SETNULL (1, "Set Null"), 
	INHERIT (2, "Inherit"), 
	CALCULATE (3, "Calculate"), 
	CALCULATE_ON_START (4, "Calculate On Start"), 
	CALCULATE_ON_UPDATE (5, "Calculate On Update"); 
	
	private String label;
	private int id;
	
	private DueDateAction(int code, String label) {
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
