package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EHtmlField;

@JsonTypeName("html")
public class KbeeEHtmlField extends EFormAbstractField<String> implements EHtmlField {
	private static final long serialVersionUID = 1L;
	
	private String editor;

	public KbeeEHtmlField() {
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Html";
	}
}