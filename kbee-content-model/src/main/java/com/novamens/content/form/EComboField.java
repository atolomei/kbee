package com.novamens.content.form;

import com.novamens.content.model.Classificable;

public interface EComboField<T> extends EFormField<T> {
	// The object defines the context
	public EFormDataSource<T> getChoicesSource(Classificable object);
}