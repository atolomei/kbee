package com.novamens.content.form;

public enum EModelType {
	
	CLASSIFIER("Classifier"),
	FORM_ATTRIBUTE("Form Attribute"),
	RESOURCE_SYSTEM ("Resource System");
	
	private String label;
	
	private EModelType(String label) {
		this.label = label;
	}
	
	public String toString() {
		return (getLabel());
	}
	
	public String getLabel() {
		return label;
	}
}