package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ETextField;

@JsonTypeName("text area")
public class KbeeETextField extends EFormAbstractField<String> implements ETextField {
	private static final long serialVersionUID = 1L;

	public KbeeETextField() {
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Text";
	}
}