package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.ECheckField;
import com.novamens.content.form.EFormData;

@JsonTypeName("check")
public class KbeeECheckField extends EFormAbstractField<Boolean> implements ECheckField {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Check";
	}
} 