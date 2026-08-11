package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ETableField;

@JsonTypeName("table")
public class KbeeETableField extends EFormAbstractField<List<?>> implements ETableField {
	private static final long serialVersionUID = 1L;

	List<EFormComponent> components = new ArrayList<EFormComponent>();
	
	public KbeeETableField() {
	}
	
	@Override
	public List<EFormComponent> getComponents() {
		return components;
	}
	
	public void setComponents(List<EFormComponent> components) {
		this.components = components;
	}
	
	public void add(EFormComponent component) {
		this.components.add(component);
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Table";
	}
}