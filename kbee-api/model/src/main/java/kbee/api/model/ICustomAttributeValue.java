package kbee.api.model;

import java.io.Serializable;

public class ICustomAttributeValue  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String attribute;
	private String value;
	
	public ICustomAttributeValue() {
	}
	
	public ICustomAttributeValue(String attribute, String value) {
		setAttribute(attribute);
		setValue(value);
	}

	public String getAttribute() {
		return attribute;
	}
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
