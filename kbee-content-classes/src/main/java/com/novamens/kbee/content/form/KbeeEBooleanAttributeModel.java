package com.novamens.kbee.content.form;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("boolean attribute")
public class  KbeeEBooleanAttributeModel extends KbeeEAttributeFieldModel<Boolean> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object value) {
		return value!=null && value instanceof Boolean ? ((Boolean)value).toString() : null;	
	}
	
	protected Boolean getValueOf(String stringvalue) {
		return Boolean.valueOf(stringvalue);
	}

	@JsonIgnore	
	@Override
	public String getModelObjectName() {
		return getModelObjectName(Locale.getDefault());
	}
}