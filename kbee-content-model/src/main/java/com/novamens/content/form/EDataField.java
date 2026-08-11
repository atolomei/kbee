package com.novamens.content.form;

// Todo campo que contenga un dato por fuera del modelo 
public interface EDataField<T> extends EFormField<T> {
	public String calculationScript();
	public EFieldSource getSource();
}
