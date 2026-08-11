package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.Locale;

import com.novamens.workflow.ProcedurePhase;

public class KbeeProcedurePhase implements ProcedurePhase, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String label;
	private String icon;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getLabel() {
		return label!=null?label:name;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel(Locale locale) {
		return label!=null?label:name;
	}
}