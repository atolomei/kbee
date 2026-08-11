package com.novamens.content.form;

import com.novamens.content.model.Attribute;

public interface EAttributeModel<T> extends EModelElementModel<T> {
	public Attribute getAttribute();
	
	static String GetTypeLabel() {
		return "Attribute";
	}
}