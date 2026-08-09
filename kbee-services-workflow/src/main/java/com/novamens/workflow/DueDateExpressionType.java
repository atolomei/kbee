package com.novamens.workflow;

public enum DueDateExpressionType {
	
	IQL (1, "IQL"), 
	JS (2, "JavaScript");
	
	private String label;
	private int id;
	
	private DueDateExpressionType(int code, String label) {
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