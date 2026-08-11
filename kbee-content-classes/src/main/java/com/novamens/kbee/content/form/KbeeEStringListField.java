package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("strings")
public class KbeeEStringListField extends KbeeEListField<String> {
	private static final long serialVersionUID = 1L;
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "String List";
	}
}