package com.novamens.whatsapp;

import java.util.ArrayList;
import java.util.List;

public class HsmComponent {
	public enum Section {
		Header,
		Body,
		Button
	}; 
	
	private Section section;
	private List<HsmParameter> parameters;
	
	public HsmComponent() {
		
	}
	
	public HsmComponent(Section section) {
		this.section = section;
	}
	
	public Section getSection() {
		return section;
	}
	
	public void setSection(Section section) {
		this.section = section;
	}
	
	public List<HsmParameter> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<HsmParameter> parameters) {
		this.parameters = parameters;
	}
	
	public void setParameters(HsmParameter... parameters) {
		this.parameters = new ArrayList<>();
		for (int p=0; p<parameters.length; p++) {
			this.parameters.add(parameters[p]);
		}
	}
}
