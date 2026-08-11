package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class ApiClassificable extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private List<IAttributeValues> seededattributes;
	private List<IAttributeValues> attributes;
	
	public List<IAttributeValues> getAttributes() {
		if (seededattributes==null) seededattributes = new ArrayList<IAttributeValues>();
		if (attributes==null) attributes = new ArrayList<IAttributeValues>();
		return !seededattributes.isEmpty() ? seededattributes : attributes;
	}
	
	public void setAttributes(List<IAttributeValues> values) {
		if (!values.isEmpty())
		this.seededattributes = values;
	}
	
	public void setAttribute(String attributename, String modelvalue) {
		if (seededattributes==null) seededattributes = new ArrayList<IAttributeValues>();
		this.seededattributes.add(new IAttributeValues(new ApiAttributeProxy(attributename), new ApiValue(modelvalue)));
	}
	
	public void removeAttribute(String attributename) {
		if (seededattributes==null) seededattributes = new ArrayList<IAttributeValues>();
		this.seededattributes.removeIf(value -> attributename.equals(value.getAttribute().getName()));
	}
	
	public void setAttribute(String attributename, ApiProxy proxyvalue) {
		if (seededattributes==null) seededattributes = new ArrayList<IAttributeValues>();
		ApiValue value = new ApiValue();
		value.setId(proxyvalue.getId());
		value.setHRef(proxyvalue.getHRef());
		this.seededattributes.add(new IAttributeValues(new ApiAttributeProxy(attributename), value));
	}
	
	public void setAttribute(String attributename, ApiValue value) {
		if (seededattributes==null) seededattributes = new ArrayList<IAttributeValues>();
		this.seededattributes.add(new IAttributeValues(new ApiAttributeProxy(attributename), value));
	}
	
	public String getAttributeValue(String attributename) {
		if (getAttributes()!=null)
		for (IAttributeValues values : getAttributes()) {
			if (values.getAttribute().getName().toLowerCase().equals(attributename.toLowerCase())) {
				return values.getValues().isEmpty() ?  null : values.getValues().get(0).getValue();
			}
		}
		return null;
	}
}
