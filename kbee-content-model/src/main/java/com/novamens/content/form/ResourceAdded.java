package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Resource;

@JsonTypeName("resource added")
public class ResourceAdded extends AbstractUpdatedField {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private Long id;
	@JsonProperty("name")
	private String name;
	
	public ResourceAdded() {
	}
	
	public ResourceAdded(EForm form, String field, Resource resource) {
		setForm(form);
		setField(field);
		setResource(resource);
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
	
	@Override
	@JsonIgnore
	public String getAction() {
		return "added in " + getField();
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		return getResourceName();
	}
	
	public String getType() {
		return "resource added";
	}
}