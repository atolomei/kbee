package com.novamens.logging;


import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.LauncherGroup;

@Entity
@DiscriminatorValue("ModelCreateEvent")
public class ModelCreateEvent extends ModelEvent {
	

	public ModelCreateEvent() {
		super();
	}

	public ModelCreateEvent(ContentTemplate template, String description) {
		super(template, description);
	}
	
	public ModelCreateEvent(DataSet dataset, String description) {
		super(dataset, description);
	}
	
	public ModelCreateEvent(Classifier classifier, String description) {
		super(classifier, description);
	}
	
	public ModelCreateEvent(Attribute attribute, String description) {
		super(attribute, description);
	}
	
	public ModelCreateEvent(LauncherGroup lg, String description) {
		super(lg, description);
	}

	
	
	public ModelCreateEvent(DataSet dataset, List<String> updatedParts) {
		super();
		setDataSet(dataset);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelCreateEvent(Classifier classifier, List<String> updatedParts) {
		super();
		setClassifier(classifier);
		setParameters(getDescription(updatedParts));
	}

	@Override
	public String getEventType() {
		return "ModelCreateEvent";
	}
	
	@Override
	public String getAction() {
		return "Create";
	}
}
