package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class ApiClassificable extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private List<ApiAttributeValues> seededattributes;
	private List<ApiAttributeValues> attributes;
	
	public List<ApiAttributeValues> getAttributes() {
		if (seededattributes==null) seededattributes = new ArrayList<ApiAttributeValues>();
		if (attributes==null) attributes = new ArrayList<ApiAttributeValues>();
		return !seededattributes.isEmpty() ? seededattributes : attributes;
	}
	
	public void setAttributes(List<ApiAttributeValues> values) {
		if (!values.isEmpty())
		this.seededattributes = values;
	}
	
	public void setAttribute(String attributename, String modelvalue) {
		if (seededattributes==null) seededattributes = new ArrayList<ApiAttributeValues>();
		this.seededattributes.add(new ApiAttributeValues(new ApiAttributeProxy(attributename), new ApiValue(modelvalue)));
	}
	
	public void removeAttribute(String attributename) {
		if (seededattributes==null) seededattributes = new ArrayList<ApiAttributeValues>();
		this.seededattributes.removeIf(value -> attributename.equals(value.getAttribute().getName()));
	}
	
	public void setAttribute(String attributename, ApiProxy proxyvalue) {
		if (seededattributes==null) seededattributes = new ArrayList<ApiAttributeValues>();
		ApiValue value = new ApiValue();
		value.setId(proxyvalue.getId());
		value.setHRef(proxyvalue.getHRef());
		this.seededattributes.add(new ApiAttributeValues(new ApiAttributeProxy(attributename), value));
	}
	
	public void setAttribute(String attributename, ApiValue value) {
		if (seededattributes==null) seededattributes = new ArrayList<ApiAttributeValues>();
		this.seededattributes.add(new ApiAttributeValues(new ApiAttributeProxy(attributename), value));
	}
	
	public String getAttributeValue(String attributename) {
		if (getAttributes()!=null)
		for (ApiAttributeValues values : getAttributes()) {
			if (values.getAttribute().getName().toLowerCase().equals(attributename.toLowerCase())) {
				return values.getValues().isEmpty() ?  null : values.getValues().get(0).getValue();
			}
		}
		return null;
	}
}
