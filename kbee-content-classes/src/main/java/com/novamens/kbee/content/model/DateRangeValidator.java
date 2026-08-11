package com.novamens.kbee.content.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.ResourceBundle;

import com.novamens.content.model.AttributeValidatable;
import com.novamens.content.model.AttributeValidator;

public class DateRangeValidator implements AttributeValidator {
	OffsetDateTime from, to;
	public boolean validate(AttributeValidatable<?> validatable) {
		
		
		boolean validate = false;
		
		try {
			if (validatable.getValue()!=null && !"".equals(validatable.getValue())) {
				Object value = validatable.getValue();
				OffsetDateTime date = null;
				if (value instanceof Date) {
					date = getTime((Date)value); 
				}
				if (date!=null) {
					validate = true;
					if (getTo()!=null && date.isAfter(getTo())) {
						validate = false;
					}
					if (validate && getFrom()!=null && date.isBefore(getFrom())) {
						validate = false;
					}
				}
			}
			else {
				validate = true;
			}
		}
		catch (Exception e) {
			
		}
		
		if (!validate) {
			ResourceBundle rb = ResourceBundle.getBundle(getClass().getName(), validatable.getLocale());
			String message = rb.getString("range-error");
			message = message.replace("%0", String.valueOf(getFrom()));
			message = message.replace("%1", String.valueOf(getTo()));
			validatable.setError(message);
		}
		
		return validate;
	}
	public void setFrom(OffsetDateTime date) {
		this.from = date;
	}
	public OffsetDateTime getFrom() {
		return from;
	}
	public void setTo(OffsetDateTime date) {
		this.to = date;
	}
	public OffsetDateTime getTo() {
		return to;
	}
	private OffsetDateTime getTime(Date date) {
		return date.toInstant().atOffset(ZoneOffset.UTC);
	}
}
