package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.ETitle;

@JsonTypeName("title")
public class KbeeETitle extends EFormAbstractComponent implements ETitle {
	private static final long serialVersionUID = 1L;

	public KbeeETitle() {
	}
	
	public KbeeETitle(String label) {
		super(label);
	}
	
	@JsonIgnore
	public String getTitle() {
		return getLabel();
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Title";
	}
}