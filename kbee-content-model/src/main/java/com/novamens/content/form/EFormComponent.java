package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME, 
	include = JsonTypeInfo.As.PROPERTY, 
	property = "type")
public interface EFormComponent {
	public String getName();
	public String getLabel();
	
	public String getSublabel();
	
	public String getCssClass();
	public boolean isEnabled(EFormData data);
	public boolean isVisible(EFormData data);
	
	public String getVisibleCondition();
	public String getEnabledCondition();
}