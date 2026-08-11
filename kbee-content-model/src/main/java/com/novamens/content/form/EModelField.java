package com.novamens.content.form;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ModelElement;

@Deprecated
public interface EModelField<T> extends EFormField<T> {
	public ModelElement getElement();
	public Classifier getParentClassifier();
}