package com.novamens.content.form;

import com.novamens.event.Event;

public interface EFormEvent extends Event {
 	public EFormField<?> getField();
	public EFormData getFormData();
}