package com.novamens.content.form;

import com.novamens.content.model.Classificable;

public interface EFormSelector<T> {
	public EFormDataSource<T> getDataSource(Classificable object);
}
