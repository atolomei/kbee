package com.novamens.content.form;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Ver ECondition KbeeEConditionValidation

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME, 
	include = JsonTypeInfo.As.PROPERTY, 
	property = "type")
public interface EValidation {
	public boolean isSubmit();
	public void validate(EValidatable validatable);
}
