package com.novamens.content.web.content.markup;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;

public class RelationMessage extends FeedbackMessage {
	private static final long serialVersionUID = 1L;
	
//	private IModel<Classifier> classifiermodel;
//	private IModel<AttributeTemplate> attributemodel;

	public RelationMessage(final Component reporter, final Serializable message, final int level) {
		super(reporter, message, level);
	}
	
//	public ClassificationMessage(final Component reporter, final AttributeTemplate attribute, final Serializable message, final int level) {
//		super(reporter, message, level);
//		setAttribute(attribute);
//	}
//	
//	public Classifier getClassifier() {
//		return classifiermodel!=null ? classifiermodel.getObject() : null;
//	}
//	
//	public void setClassifier(Classifier classifier) {
//		classifiermodel = new ObjectModel<Classifier>(classifier);
//	}
//	
//	public void setAttribute(AttributeTemplate attribute) {
//		attributemodel = new ObjectModel<AttributeTemplate>(attribute);
//	}
//	
//	public AttributeTemplate getAttribute() {
//		return attributemodel!=null ? attributemodel.getObject() : null;
//	}
//	
//	@Override
//	public void detach() {
//		if (attributemodel!=null) attributemodel.detach();
//		if (classifiermodel!=null) classifiermodel.detach();
//	}

}
