package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Resource;

@JsonTypeName("resource moved")
public class ResourceMoved extends AbstractUpdatedField {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private Long id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("to")
	private String to;
	
	public ResourceMoved() {
	}
	
	public ResourceMoved(EForm form, String field, Resource resource, String to) {
		setForm(form);
		setField(field);
		setResource(resource);
		setTo(to);
	}
	
	public void setResource(Resource resource) {
		this.id = (Long)resource.getId();
		this.name = resource.getName();
	}
	
	@JsonProperty("id")
	public Long getResourceId() {
		return id;
	}
	
	@JsonProperty("name")
	public String getResourceName() {
		return name;
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@Override
	@JsonIgnore
	public String getAction() {
		return "moved to " + getTo();
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		return getResourceName();
	}
	
	public String getType() {
		return "resource moved";
	}
	
	public boolean same(UpdatedField field) {
		if (!(field instanceof ResourceMoved)) return false;
		return ((ResourceMoved)field).getTo().equals(getTo());
	}

}