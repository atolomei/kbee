package com.novamens.content.form;

import com.novamens.content.model.Classificable;

public interface EAutoCompleteField<T> extends EFormField<T> {
	// The object defines the context
	public EFormDataSource<T> getChoicesSource(Classificable context);
	public String getValueTemplate();
	public String getInfoTemplate();
	public String getChoiceTemplate();
}