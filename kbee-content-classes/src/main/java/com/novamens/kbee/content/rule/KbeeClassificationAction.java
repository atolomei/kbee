package com.novamens.kbee.content.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ClassificationAction;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.service.ServiceLocator;

public class KbeeClassificationAction extends KbeeAbstractAction implements ClassificationAction, Serializable  {
	private static final long serialVersionUID = 1L;
	
	private Serializable classifierId;
	private List<Serializable> values = new ArrayList<Serializable>();
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Object execute(Content content) {
		if (!classified(content)) {
			content.setClassification(getClassifier(), getValues(content));
			List<String> values = new ArrayList<String>();
			getValues().forEach(value -> values.add(value.getDisplayName()));
			content.getService(ContentService.class).update(getUpdateLabel(values));
		}
		return content;
	}
	
	public void setValues(List<DataSetMember> values) {
		this.values = new ArrayList<Serializable>();
		values.forEach( value -> this.values.add(value.getId()));
	}
	
	public void setValues(Collection<DataSetMember> values) {
		this.values = new ArrayList<Serializable>();
		values.forEach( value -> this.values.add(value.getId()));
	}
	
	public List<DataSetMember> getValues() {
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		ArrayList<DataSetMember> values = new ArrayList<DataSetMember>();
		for (Serializable valueId : this.values) {
			KbeeDataSetMember member = (KbeeDataSetMember)sf.getCurrentSession().load(KbeeDataSetMember.class, valueId);
			if (member!=null) values.add(member);
		}
		return values;
	}
		
	public Classifier getClassifier() {
		if (classifierId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		KbeeClassifier classifier = (KbeeClassifier)sf.getCurrentSession().load(KbeeClassifier.class, this.classifierId);
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifierId = classifier.getId();
	}
	
	protected boolean classified(Content content) {
		boolean classified = false;
		for (DataSetMember value : getValues()) {
			classified = false;
			for (Classification classification : content.getClassification()) {
				if (classification!=null && 
					classification.getClassifier().equals(getClassifier()) &&
					value.equals(classification.getDataSetMember())) {
					classified = true;
					break;
				}
			}
			if (!classified) 
				break;
		}
		return classified;
	}
	
	protected String getUpdateLabel(List<String> values) {
		ResourceBundle res = ResourceBundle.getBundle(Action.class.getName(), Locale.getDefault());
		String label =  getClassifier().getDisplayName();
		String valueslabel = "";
		for (String value : values) {
			if (!"".equals(valueslabel)) valueslabel += ", ";
			valueslabel += value.trim();
		}
		label += " " + valueslabel + " " + res.getString("update-label");
		return label;
	}
	
	private List<DataSetMember> getValues(Content content) {
		List<DataSetMember> values = new ArrayList<DataSetMember>();
		List<Classification> classifications = content.getClassification(getClassifier());
		classifications.forEach(classification -> values.add(classification.getDataSetMember()));
		for (DataSetMember value : getValues()) {
			if (!values.contains(value)) values.add(value);
		}
		return values;
	}

}
