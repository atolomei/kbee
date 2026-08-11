package com.novamens.content.form;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;

public interface EClassifierSelector extends EFormSelector<DataSetMember> {
	public Classifier getParentClassifier();
}