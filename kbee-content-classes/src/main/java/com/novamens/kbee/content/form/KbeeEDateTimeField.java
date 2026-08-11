package com.novamens.kbee.content.form;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EDateField;
import com.novamens.content.form.EFormData;

@JsonTypeName("datetime")
public class KbeeEDateTimeField extends EFormAbstractField<OffsetDateTime> implements EDateField {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "DateTime";
	}
}