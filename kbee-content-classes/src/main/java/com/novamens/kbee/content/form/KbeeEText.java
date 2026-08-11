package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EComponentType;
import com.novamens.content.form.EText;

@JsonTypeName("text")
public class KbeeEText extends EFormAbstractComponent implements EText{
	private static final long serialVersionUID = 1L;
	
	private String text;

	public KbeeEText() {
	}
	
	public KbeeEText(String label) {
		super(label);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@JsonIgnore
	public EComponentType getType() {
		return EComponentType.STATIC_TEXT;
	}
	
	@Override
	public String getTypeLabel() {
		return getType().getLabel();
	}
}