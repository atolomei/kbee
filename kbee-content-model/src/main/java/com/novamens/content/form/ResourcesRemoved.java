package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("resources removed")
public class ResourcesRemoved extends AbstractUpdatedField {
	private static final long serialVersionUID = 1L;
	
	public ResourcesRemoved() {
	}
	
	public ResourcesRemoved(EForm form, String field) {
		setForm(form);
		setField(field);
	}
	
	@Override
	@JsonIgnore
	public String getAction() {
		return "Removed all from " + getField();
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		return "";
	}
	
	public String getType() {
		return "resources removed";
	}
}