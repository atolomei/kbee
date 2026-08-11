package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME, 
	include = JsonTypeInfo.As.PROPERTY, 
	property = "type")
public abstract class AbstractUpdatedField implements UpdatedField {
	private static final long serialVersionUID = 1L;
	
	private String field;
	private EForm form;
	
	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public void setForm(EForm form) {
		this.form = form;
	}
	
	@Override
	@JsonIgnore
	public EForm getForm() {
		return form;
	}
	
	public boolean same(UpdatedField field) {
		return getClass().equals(field.getClass());
	}
}