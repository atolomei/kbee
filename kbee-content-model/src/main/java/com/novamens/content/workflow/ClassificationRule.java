package com.novamens.content.workflow;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;

public interface ClassificationRule extends WorkflowRule {
	public Classifier getClassifier();
	public DataSetMember getValue();
}
