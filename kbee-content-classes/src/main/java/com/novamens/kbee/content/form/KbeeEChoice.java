package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormChoice;
import com.novamens.content.form.EFormData;

@JsonTypeName("choice")
public class KbeeEChoice extends EFormAbstractField<Boolean> implements EFormChoice  {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
}
