package com.novamens.kbee.content.base;

import java.io.Serializable;

import com.novamens.content.base.CustomAttribute;

public class KbeeCustomAttribute implements CustomAttribute, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String value;
	
	public KbeeCustomAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeCustomAttribute)) return false;
		return ((KbeeCustomAttribute)object).getName().equals(getName()) && ((KbeeCustomAttribute)object).getValue().equals(getValue());
	}
}