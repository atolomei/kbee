package com.novamens.content.text.template;

import java.io.Serializable;
import java.util.List;

public class Variable implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int offset, length;
	private String name;
	private String attribute;
	private String type;
	private String valueType;
	private String defaultValue;
	private String format;
	private List<String> options;

	public Variable() {
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
	
		if (name!=null)
			str.append(name);
		
		
		if (attribute!=null) {
			if (str.length()>0)
				str.append(" | ");
			str.append(attribute);
		}
		

		if (type!=null) {
			if (str.length()>0)
				str.append(" | ");
			str.append(type);
		}

		
		if (valueType!=null) {
			if (str.length()>0)
				str.append(" | ");

			str.append(valueType);
		}
		

		if (defaultValue!=null) {
			if (str.length()>0)
				str.append(" | ");
			str.append(defaultValue);
		}


		if (format!=null) {
			if (str.length()>0)
				str.append(" | ");
			str.append(format);
		}

		
		return str.toString();
		
	}
	
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setValueType(String type) {
		this.valueType = type;
	}
	
	public String getValueType() {
		return valueType;
	}
	
	public void setDefaultValue(String value) {
		this.defaultValue = value;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAttribute() {
		return attribute;
	}
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getValue() {
		return null;
	}
	
	public List<String> getOptions() {
		return options;
	}
	
	public void setOptions(List<String> options) {
		this.options = options;
	}
	
	public void setOffset(int value) {
		this.offset = value;
	}
	
	public int getOffset() {
		return offset;
	}
	public void setLength(int value) {
		this.length = value;
	}
	
	public int getLength() {
		return length;
	}
}
