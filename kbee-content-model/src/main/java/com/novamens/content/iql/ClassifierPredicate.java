package com.novamens.content.iql;

import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.iql.CalculatedPredicate;

public interface ClassifierPredicate extends CalculatedPredicate {
	public Classifier getClassifier();
	public List<DataSetMember> getMembers(String argument);
}
