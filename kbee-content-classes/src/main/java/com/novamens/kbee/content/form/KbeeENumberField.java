package com.novamens.kbee.content.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ENumberField;
import com.novamens.content.form.EValidation;

@JsonTypeName("number")
public class KbeeENumberField extends EFormAbstractField<String> implements ENumberField {
	private static final long serialVersionUID = 1L;
	
	// Build form data from object
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	public List<EValidation> getValidations() {
		List<EValidation> validations = super.getValidations();
		boolean numeric = false;
		if (validations!=null) {
			for (EValidation validation : validations) {
				if (validation instanceof KbeeENumericValidation) {
					numeric = true;
					break;
				}
			}
			if (!numeric) {
				validations.add(new KbeeENumericValidation());
			}
		}
		return validations;
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Number";
	}
} 