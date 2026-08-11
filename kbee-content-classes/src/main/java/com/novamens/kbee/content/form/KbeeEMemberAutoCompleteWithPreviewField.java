package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("member autocomplete preview")
public class KbeeEMemberAutoCompleteWithPreviewField extends KbeeEMemberAutoCompleteField {
	private static final long serialVersionUID = 1L;
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "AutoComplete (Preview)";
	}
}