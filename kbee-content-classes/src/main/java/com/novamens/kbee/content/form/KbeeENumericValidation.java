package com.novamens.kbee.content.form;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;

@JsonTypeName("numeric")
public class KbeeENumericValidation implements EValidation, Serializable {
	private static final long serialVersionUID = 1L;
	
	public KbeeENumericValidation() {
	}
	
	@JsonIgnore
	public boolean isSubmit() {
		return true;
	}
	
	public void validate(EValidatable validatable) {
		Object data =  validatable.getData().getData(validatable.getField());
		if (!(data==null || isNumber(data.toString()))) {
			validatable.error("error.not_number");
		}
	}
	
	private boolean isNumber(String argument) {
		for (int c = 0; c < argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
	
					
	private boolean isFloat(String argument) {
		try {
			Double val=Double.valueOf(argument);
			return true;
		} catch (Exception e) {
			return false;
		}
		
	}
	
}