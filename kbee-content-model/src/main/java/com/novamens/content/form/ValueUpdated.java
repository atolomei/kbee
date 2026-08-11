package com.novamens.content.form;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.security.Identifiable;

@JsonTypeName("value")
public class ValueUpdated extends AbstractUpdatedField{
	private static final long serialVersionUID = 1L;
	
	Serializable oldvalue, newvalue;
	
	public ValueUpdated() {
	}
	
	public ValueUpdated(EForm form, String field, Object oldvalue, Object newvalue) {
		setField(field);
		setForm(form);
		setNewValue(newvalue);
		setOldValue(oldvalue);
	}
	
	public void setNewValue(Object value) {
		if (value instanceof Identifiable) {
			newvalue = ((Identifiable)value).getId();
		}
		else {
			if (value instanceof OffsetDateTime) {
				newvalue = value.toString();
			}
			else
			if (value instanceof Serializable) {
				newvalue = (Serializable)value;
			}
			else {
				if (value!=null) {
					newvalue = value.toString();
				}
				else {
					newvalue = null;
				}
			}
		}
	}
	
	public Serializable getNewValue() {
		return newvalue;
	}
	
	public void setOldValue(Object value) {
		if (value instanceof Identifiable) {
			oldvalue = ((Identifiable)value).getId();
		}
		else {
			if (value instanceof OffsetDateTime) {
				oldvalue = value.toString();
			}
			else
			if (value instanceof Serializable) {
				oldvalue = (Serializable)value;
			}
			else {
				if (value!=null) {
					oldvalue = value.toString();
				}
				else {
					oldvalue = null;
				}
			}
		}
	}
	
	public Serializable getOldValue() {
		return oldvalue;
	}
	
	@Override
	@JsonIgnore
	public String getLabel() {
		return getField();
	}
	
	@Override
	@JsonIgnore
	public String getAction() {
		return "Updated";
	}
	
	public String getType() {
		return "value";
	}
}