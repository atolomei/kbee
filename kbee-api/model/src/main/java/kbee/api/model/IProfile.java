package kbee.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IProfile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private List<ICustomAttributeValue> attributes = null;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<ICustomAttributeValue> getAttributes() {
		if (this.attributes == null)
			this.attributes = new ArrayList<ICustomAttributeValue>();
		return attributes;
	}
	
	public String getValue(String attribute) {
		for (ICustomAttributeValue value : getAttributes()) {
			if (value.getAttribute().equals(attribute)) {
				return value.getValue();
			}
		}
		return null;
	}
	
	public void setAttribute(String attribute, String value) {
		if (this.attributes == null)
			this.attributes = new ArrayList<ICustomAttributeValue>();
		this.attributes.add(new ICustomAttributeValue(attribute, value));
	}
}
