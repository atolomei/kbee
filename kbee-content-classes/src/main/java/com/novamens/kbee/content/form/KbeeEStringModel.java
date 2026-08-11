package com.novamens.kbee.content.form;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("string")
public class  KbeeEStringModel extends KbeeEFormAttributeModel<String> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected String getValueOf(String stringvalue) {
		return stringvalue;
	}
	
	@Override
	@JsonIgnore 
	public String getDescription(Locale locale) {
		return "String";
	}
	
	@JsonIgnore 
	@Override
	public String getModelObjectName(Locale locale) {
		return "String";
	}
}