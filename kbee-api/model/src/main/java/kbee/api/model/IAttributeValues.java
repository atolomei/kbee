package kbee.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IAttributeValues  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ApiAttributeProxy attribute;
	private List<ApiValue> values = new ArrayList<ApiValue>();

	public IAttributeValues() {
	}
	
	public IAttributeValues(ApiAttributeProxy attribute) {
		setAttribute(attribute);
	}
	
	public IAttributeValues(ApiAttributeProxy attribute, ApiValue value) {
		setAttribute(attribute);
		setValue(value);
	}
	
	public ApiAttributeProxy getAttribute() {
		return attribute;
	}
	
	public void setAttribute(ApiAttributeProxy attribute) {
		this.attribute = attribute;
	}
	
	public List<ApiValue> getValues() {
		return values;
	}
	
	public void setValue(ApiValue value) {
		this.values.add(value);
	}
	
	public void addValue(ApiValue value) {
		this.values.add(value);
	}
}