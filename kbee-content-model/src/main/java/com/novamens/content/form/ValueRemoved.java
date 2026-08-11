package com.novamens.content.form;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.security.Identifiable;

@JsonTypeName("value removed")
public class ValueRemoved extends AbstractUpdatedField {
	private static final long serialVersionUID = 1L;
	
	Serializable value;
	
	public ValueRemoved() {
	}
	
	public ValueRemoved(EForm form, String field, Object value) {
		setForm(form);
		setField(field);
		setValue(value);
	}
	
	public void setValue(Object value) {
		if (value instanceof Identifiable) {
			this.value = ((Identifiable)value).getId();
		}
		else {
			if (value instanceof OffsetDateTime) {
				this.value = value.toString();
			}
			else
			if (value instanceof Serializable) {
				this.value = (Serializable)value;
			}
			else {
				if (value!=null) {
					this.value = value.toString();
				}
				else {
					this.value = null;
				}
			}
		}
	}
	
	@Override
	@JsonIgnore
	public String getAction() {
		return "Removed";
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		return getField();
	}
	
	public String getType() {
		return "value removed";
	}
}