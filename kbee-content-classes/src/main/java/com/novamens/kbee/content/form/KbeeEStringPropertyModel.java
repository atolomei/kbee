package com.novamens.kbee.content.form;

import java.util.Locale;
import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("string property")
public class  KbeeEStringPropertyModel extends KbeeEPropertyFieldModel<String> {
	private static final long serialVersionUID = 1L;
	
	public KbeeEStringPropertyModel() {
		
	}
	
	public KbeeEStringPropertyModel(String property) {
		setProperty(property);
	}
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected String getValueOf(String stringvalue) {
		return stringvalue;
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle( KbeeEAttributeFieldModel.class.getName(), locale);
		return res.getString("string");
	}
}