package com.novamens.content.model;

import java.util.List;
import java.util.Map;

import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;

public interface Classificable extends Auditable, Identifiable {
	
	public List<Classification> getClassification();
	public List<Classification> getClassification(Classifier classifier);
	
	public void setClassification(Classifier classifier, List<DataSetMember> members);
	public void setClassification(Classifier classifier, DataSetMember member);

	public void addClassification(Classification classification);
	
	
	public void addClassification(Classifier c, DataSetMember object);
				
	public void removeAllClassification(Classifier classifier);
	public void removeClassification(Classification c);
	
	public void setAttributeValues(Attribute name, List<String> values);
	public List<String> getAttributeValues(Attribute name);

	public Map<String, List<String>> getAttributesAsMap();
	
	public List<Classifier> getClassifiers();

}
