package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EHtmlStructField;

@JsonTypeName("htmlstruct")
public class KbeeEHtmlStructField extends KbeeEHtmlField implements EHtmlStructField {
	private static final long serialVersionUID = 1L;

	public KbeeEHtmlStructField() {
	}

	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "HtmlStruct";
	}
}