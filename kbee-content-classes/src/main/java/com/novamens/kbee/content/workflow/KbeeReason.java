package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import com.novamens.workflow.Reason;

public class KbeeReason implements Reason, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String code;
	private String label;

	public KbeeReason() {
		
	}
	
	public KbeeReason(String codel, String label) {
		setCode(code);
		setLabel(label);
	}
	
	public String getCode() {
		return code;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String setCode() {
		return code;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
}