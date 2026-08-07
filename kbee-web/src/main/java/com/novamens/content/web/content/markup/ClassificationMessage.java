package com.novamens.content.web.content.markup;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.wicket.model.ObjectModel;

public class ClassificationMessage extends FeedbackMessage {
	private static final long serialVersionUID = 1L;
	
	private IModel<Classifier> classifiermodel;
	private IModel<Attribute> attributemodel;

	public ClassificationMessage(final Component reporter, final Classifier classifier, final Serializable message, final int level) {
		super(reporter, message, level);
		setClassifier(classifier);
	}
	
	public ClassificationMessage(final Component reporter, final AttributeTemplate attribute, final Serializable message, final int level) {
		super(reporter, message, level);
		setAttribute(attribute);
	}
	
	public Classifier getClassifier() {
		return classifiermodel!=null ? classifiermodel.getObject() : null;
	}
	
	public void setClassifier(Classifier classifier) {
		classifiermodel = new ObjectModel<Classifier>(classifier);
	}
	
	public void setAttribute(AttributeTemplate template) {
		attributemodel = new ObjectModel<Attribute>(template.getAttribute());
	}
	
	public AttributeTemplate getAttribute() {
		return attributemodel!=null ? new KbeeAttributeTemplate(attributemodel.getObject()) : null;
	}
	
	@Override
	public void detach() {
		if (attributemodel!=null) attributemodel.detach();
		if (classifiermodel!=null) classifiermodel.detach();
	}

}
