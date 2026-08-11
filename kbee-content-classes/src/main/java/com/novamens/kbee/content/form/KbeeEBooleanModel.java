package com.novamens.kbee.content.form;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("boolean")
public class  KbeeEBooleanModel extends KbeeEFormAttributeModel<Boolean> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected Boolean getValueOf(String stringvalue) {
		return Boolean.valueOf(stringvalue);
	}
	
	@Override
	@JsonIgnore 
	public String getDescription(Locale locale) {
		return "Boolean";
	}
	
	@JsonIgnore 
	@Override
	public String getModelObjectName(Locale locale) {
		return "Boolean";
	}
}