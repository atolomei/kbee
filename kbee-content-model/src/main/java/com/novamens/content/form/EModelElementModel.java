package com.novamens.content.form;

import com.novamens.content.model.Classifier;

public interface EModelElementModel<T> extends EFieldModel<T> {
	public Classifier getParentClassifier();
}