package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormSection;

@JsonTypeName("section")
public class KbeeEFormSection extends EFormAbstractComponent implements EFormSection {
	private static final long serialVersionUID = 1L;
	
	List<EFormComponent> components = new ArrayList<EFormComponent>();
	
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
	@JsonIgnore
	public String getTypeLabel() {
		return "Section";
	}
}
