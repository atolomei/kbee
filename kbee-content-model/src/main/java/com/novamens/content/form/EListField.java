package com.novamens.content.form;

import com.novamens.content.model.Classificable;

public interface EListField<T> extends EFormField<T> {
	
	// The object defines the context
	public EFormDataSource<?> getChoicesSource(Classificable object);
	
	public String getValueTemplate();
	public String getInfoTemplate();
	public String getChoiceTemplate();
}