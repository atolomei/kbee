package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;

@JsonTypeName("multiplicity")
public class KbeeEMultipicityValidation implements EValidation, Serializable {
	private static final long serialVersionUID = 1L;
	
	public KbeeEMultipicityValidation() {
	}
	
	@JsonIgnore
	public boolean isSubmit() {
		return true;
	}
	
	public void validate(EValidatable validatable) {
		Object data =  validatable.getData().getData(validatable.getField());
		if ((data instanceof String && data!=null && "".equals( ((String)data).trim() )	|| 
			data==null || 
			(data instanceof Collection<?> && ((Collection<?>)data).isEmpty()))) {
			validatable.error("error.required");
		}
	}
}