package com.novamens.kbee.content.model;

import com.novamens.content.model.ModelReference;

public class KbeeModelReference implements ModelReference {
	private static final long serialVersionUID = 1L;
	
	private String description;
	private String url;
	private String group;
	private String object;
	
	private String modelElemetClass;
	

	@Override	
	public String getModelElementClass() {
		return modelElemetClass;
	}
	
	public void setModelElementClass(String object) {
		this.modelElemetClass = object;
	}
	
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override	
	public String getDescription() {
		return description;
	}
	
	@Override	
	public String getObject() {
		return object;
	}
	
	public void setObject(String object) {
		this.object = object;
	}
	
	@Override	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String object) {
		this.group = object;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@Override	
	public String getUrl() {
		return url;
	}
}