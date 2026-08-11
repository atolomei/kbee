package com.novamens.kbee.content.model;

import java.util.ResourceBundle;

import com.novamens.content.model.AttributeValidatable;
import com.novamens.content.model.AttributeValidator;


public class NumericRangeValidator implements AttributeValidator {
	long from, to;
	public boolean validate(AttributeValidatable<?> validatable) {
		
		boolean validate = false;
		
		try {
			if (validatable.getValue()!=null && !"".equals(validatable.getValue())) {
				long  value = Long.valueOf(String.valueOf(validatable.getValue()));
				validate = value>=getFrom() && value<=getTo();
			}
			else {
				validate = true;
			}
		}
		catch (Exception e) {
			
		}
		
		if (!validate) {
			ResourceBundle rb = ResourceBundle.getBundle(NumericRangeValidator.this.getClass().getName(), validatable.getLocale());
			String message = rb.getString("range-error");
			message = message.replace("%0", String.valueOf(getFrom()));
			message = message.replace("%1", String.valueOf(getTo()));
			validatable.setError(message);
		}
		
		return validate;
	}
	public void setFrom(long date) {
		this.from = date;
	}
	public long getFrom() {
		return from;
	}
	public void setTo(long value) {
		this.to = value;
	}
	public long getTo() {
		return to;
	}
}
