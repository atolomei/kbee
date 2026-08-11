package com.novamens.kbee.content.qa;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.qa.QAControl;
import com.novamens.content.qa.QAService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.domain.KbeeDomain;

public class ContentQAService implements QAService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentQAService.class.getName());

	
	private Content content = null;

	public ContentQAService() {
	}
	
	public ContentQAService(Content content) {
		this.content = content;
	}
	
	public QAControl eval() {
		
		return new QAControl();
		
		/*
		int qastate = ((KbeeContent)getContent()).getQAState();
		QAControl qacontrol = new QAControl();
		if (qastate == QAControl.ERROR) { 
			qacontrol.status = QAControl.ERROR;
			//qacontrol.message = ((KbeeContent)getContent()).getQAMessage();
		}
		if (qastate == QAControl.ALERT) { 
			qacontrol.status = QAControl.ALERT;
			//qacontrol.message = ((KbeeContent)getContent()).getQAMessage();
		}
		return qacontrol;
		*/
	}
	
	public QAControl eval(List<Classification> contentclassification) {
		StringBuilder qamessage = new StringBuilder();
		QAControl qacontrol = new QAControl();
		ContentTemplate template = getContent().getContentTemplate(); 
		for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
			Classifier classifier = classifiertemplate.getClassifier();
			if (classifier.isMandatory()) {
				boolean classified = false;
				for (Classification classification : contentclassification) {
					if (classification!=null && classification.getClassifier().equals(classifier)) {
						classified = true;
						break;
					}
				}
				if (!classified) {
					if (qamessage.length()>0) 
						qamessage.append(", ");
					qamessage.append(classifier.getName());
				}
			}
		}
		for (AttributeTemplate attributetemplate : template.getAttributes()) {
			if (attributetemplate.getAttribute().isRequired()) {
				if (getContent().getAttributeValues(attributetemplate.getAttribute()).isEmpty()) {
					if (qamessage.length()>0) 
						qamessage.append(", ");
					qamessage.append(attributetemplate.getAttribute().getName());
				}
			}
		}
		if (qamessage.length()==0) {
			qacontrol.status = QAControl.OK;
		}
		else {
			qacontrol.status = QAControl.ERROR;
		}
		return qacontrol;
	}
	
	public void update() {
		
		logger.error("ContentQAService is not longer used.");
		return;
		/*
		StringBuilder qamessage = new StringBuilder();
		ContentTemplate template = getContent().getContentTemplate(); 
		for (ClassifierTemplate classifiertemplate : template.getClassifiers()) {
			Classifier classifier = classifiertemplate.getClassifier();
			if (classifier.isMandatory()) {
				boolean classified = false;
				for (Classification classification : getContent().getClassification()) {
					if (classification!=null && classification.getClassifier().equals(classifier)) {
						classified = true;
						break;
					}
				}
				if (!classified) {
					if (qamessage.length()>0) 
						qamessage.append(", ");
					qamessage.append(classifier.getName());
				}
			}
		}
		for (AttributeTemplate attributetemplate : template.getAttributes()) {
			if (attributetemplate.getAttribute()!=null && attributetemplate.getAttribute().isRequired()) {
				if (getContent().getAttributeValues(attributetemplate.getAttribute()).isEmpty()) {
					if (qamessage.length()>0) 
						qamessage.append(", ");
					qamessage.append(attributetemplate.getAttribute().getName());
				}
			}
		}
		if (qamessage.length()==0) {
			((KbeeContent)getContent()).setQAState(QAControl.OK);
			((KbeeContent)getContent()).setQAMessage(null);
		}
		else {
			((KbeeContent)getContent()).setQAState(QAControl.ERROR);
			((KbeeContent)getContent()).setQAMessage(qamessage.toString());
		}
		*/
	}
	
	public Content getContent() {
		return content;
	}
}
