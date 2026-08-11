package com.novamens.content.form;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;

public interface EClassifierModel<T> extends EModelElementModel<T> {
	public Classifier getClassifier();
	public ClassifierTemplate getRelationTemplate(Classificable object);
	public AccessStrategy getAccessStrategy();
	public String getIql();
	
	static String GetTypeLabel() {
		return "Classifier";
	}
}