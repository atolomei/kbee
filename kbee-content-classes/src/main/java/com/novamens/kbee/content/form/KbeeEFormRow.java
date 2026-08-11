package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EComponentType;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;

@JsonTypeName("row")
public class KbeeEFormRow extends EFormAbstractComponent implements EFormContainer {
	private static final long serialVersionUID = 1L;

	List<EFormComponent> components = new ArrayList<EFormComponent>();
	
	public KbeeEFormRow() {
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
	
	@JsonIgnore
	public EComponentType getType() {
		return EComponentType.ROW;
	}
	
	@Override
	public String getTypeLabel() {
		return getType().getLabel();
	}
}