package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("number attribute")
public class  KbeeENumberAttributeModel extends KbeeEAttributeFieldModel<String> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected String getValueOf(String stringvalue) {
		return stringvalue;
	}
}