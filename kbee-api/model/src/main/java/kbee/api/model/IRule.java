package kbee.api.model;

import java.io.Serializable;

public class IRule implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String type;
	private ApiProxy classifier;
	private ApiProxy attribute;
	private ApiProxy value;
	private String stringValue;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ApiProxy getClassifier() {
		return classifier;
	}
	public void setClassifier(ApiProxy classifier) {
		this.classifier = classifier;
	}
	public ApiProxy getAttribute() {
		return attribute;
	}
	public void setAttribute(ApiProxy attribute) {
		this.attribute = attribute;
	}
	public ApiProxy getValue() {
		return value;
	}
	public void setValue(ApiProxy value) {
		this.value = value;
	}
	public String getStringValue() {
		return stringValue;
	}
	public void setStringValue(String stringvalue) {
		this.stringValue = stringvalue;
	}
}