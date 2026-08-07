package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;

@Entity
@DiscriminatorValue("ModelDeleteEvent")
public class ModelDeleteEvent extends ModelEvent {
	
	 

	public ModelDeleteEvent() {
		super();
	}
	
	public ModelDeleteEvent(DataSet dataset, String description) {
		super(dataset, description);
	}
	
	public ModelDeleteEvent(Classifier classifier, String description) {
		super(classifier, description);
	}
	
	public ModelDeleteEvent(Attribute attribute, String description) {
		super(attribute, description);
	}
		
	public ModelDeleteEvent(DataSet dataset, List<String> updatedParts) {
		super();
		setDataSet(dataset);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelDeleteEvent(Classifier classifier, List<String> updatedParts) {
		super();
		setClassifier(classifier);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelDeleteEvent(Attribute attribute, List<String> updatedParts) {
		super();
		setAttribute(attribute);
		setParameters(getDescription(updatedParts));
	}

	public ModelDeleteEvent(ContentTemplate template, String description) {
		super(template, description);
	}
	
	@Override
	public String getEventType() {
		return "ModelDeleteEvent";
	}
	
	@Override
	public String getAction() {
		return "Delete";
	}

}
