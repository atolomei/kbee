package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("member tree autocomplete")
public class KbeeEMemberAutoCompleteTreeField extends KbeeEMemberAutoCompleteField {
	private static final long serialVersionUID = 1L;

	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "AutoCompleteTree";
	}
}