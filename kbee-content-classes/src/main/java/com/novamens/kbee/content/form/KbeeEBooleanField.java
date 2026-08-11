package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EBooleanField;
import com.novamens.content.form.EFormData;

@JsonTypeName("boolean")
public class KbeeEBooleanField extends EFormAbstractField<Boolean> implements EBooleanField {
	private static final long serialVersionUID = 1L;
	
	
	// Build form data from object
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Boolean";
	}
} 