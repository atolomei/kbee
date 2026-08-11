package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("string attribute")
public class  KbeeEStringAttributeModel extends KbeeEAttributeFieldModel<String> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected String getValueOf(String stringvalue) {
		return stringvalue;
	}
}