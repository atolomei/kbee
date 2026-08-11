package com.novamens.kbee.content.form;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

// Json Format example : { "type": "condition", "condition", "script", "message", "message" }
// The evaluation context contains a variable for each field of the form plus:
// "today" con la fecha del día

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;

@JsonTypeName("condition")
public class KbeeEConditionValidation implements EValidation, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String condition;
	private String message;
	
	public KbeeEConditionValidation() {
	}
	
	public KbeeEConditionValidation(String condition, String message) {
		this.condition = condition;
		this.message = message;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String condition) {
		this.message = condition;
	}
	
	@JsonIgnore
	public boolean isSubmit() {
		return false;
	}
	
	public void validate(EValidatable validatable) {
		ScriptEvaluator evaluator = new ScriptEvaluator();
		evaluator.setBinding(validatable.getField(), validatable.getValue());
		Object evaluation =  evaluator.evaluate(getCondition(), validatable.getData());
		if (Boolean.FALSE.equals(evaluation)) {
			validatable.error(getMessage());
		}
	}
}