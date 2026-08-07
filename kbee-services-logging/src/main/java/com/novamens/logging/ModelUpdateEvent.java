package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.RelationTemplate;


/** -----------------------------------------------------------------------------------
 * 
 *  {@link UserLabel}
 *  {@link DataSetMember}

 *  {@link DataSet}
 *  {@link Classifier}
 *  {@link Attribute}
 *  {@link ContentTemplate}
 *  
 *  {@link TemplateRelation}
 *  
 */			

@Entity
@DiscriminatorValue("ModelUpdateEvent")
public class ModelUpdateEvent extends ModelEvent {

	public ModelUpdateEvent() {
		super();
		
	}

	public ModelUpdateEvent(RelationTemplate clase, String des) {
		super(clase, des);
	}
	
	public ModelUpdateEvent(RelationTemplate clase, List<String> updatedParts) {
		super(clase, updatedParts);
	}
	
	public ModelUpdateEvent(ContentTemplate clase, String des) {
		super(clase, des);
	}
	
	public ModelUpdateEvent(ContentTemplate clase, List<String> updatedParts) {
		super(clase, updatedParts);
	}
	
	public ModelUpdateEvent(DataSet dataset,String des) {
		super(dataset, des);
	}

	public ModelUpdateEvent(DataSet dataset, List<String> updatedParts) {
		super();
		setDataSet(dataset);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelUpdateEvent(Attribute attribute,String des) {
		super(attribute, des);
	}

	public ModelUpdateEvent(Attribute attribute, List<String> updatedParts) {
		super();
		setAttribute(attribute);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelUpdateEvent(Classifier classifier, List<String> updatedParts) {
		super();
		setClassifier(classifier);
		setParameters(getDescription(updatedParts));
	}
	
	public ModelUpdateEvent(Classifier object, String des) {
		super(object, des);
	}

	@Override
	public String getEventType() {
		return "ModelUpdateEvent";
	}
	
	@Override
	public String getAction() {
		return "Update";
	}
}
