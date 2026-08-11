package com.novamens.content.form;

public interface EValidatable {
	public Object getValue();
	public EFormField<?> getField();
	public EFormData getData();
	public void error(String message);
	public void error(String message, String... parameter);
}