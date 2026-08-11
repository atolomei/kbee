package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeValueMember;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

public class KbeeClassificationRule implements ClassificationRule, Serializable {
	private static final long serialVersionUID = 1L;

	private Serializable classifierId;
	private Serializable valueId;
	
	public KbeeClassificationRule() {
		
	}
	
	public KbeeClassificationRule(Classifier classifier, DataSetMember value) {
		setClassifier(classifier);
		setValue(value);
	}
	
	public void execute(WorkflowContext context) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		members.add(getValue());
		
		Content content = ((KbeeContext)context).getContent();
		
		boolean editableTitle = true;
		
		if (content.getContentTemplate().isTitleEditable()) {
			if ("true".equals(content.getService(PropertyService.class).getProperty("title"))) {
				editableTitle = false;
			}	
			else {
				if (content instanceof Versionable) {
					@SuppressWarnings("unchecked")
					Content previousversion = ((Versionable<Content>)content).getPreviousVersion();
					if (previousversion!=null && "true".equals(previousversion.getService(PropertyService.class).getProperty("title"))) {
						content.getService(PropertyService.class).setProperty("title", "true");
						editableTitle = false;
					}
				}
			}
		}
		
		content.setClassification(getClassifier(), members);
		
		if (editableTitle) {
			ExtractionRule titleRule = content.getContentTemplate().getTitleRule();
			String title = (String)titleRule.extract(content);
			content.setTitle(title);
		}
	}
	
	public Classifier getClassifier() {
		if (classifierId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Classifier classifier = (Classifier)sf.getCurrentSession().load(KbeeClassifier.class, this.classifierId);
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifierId = classifier!=null ? classifier.getId() : null;
	}
	
	public DataSetMember getValue() {
		if (valueId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		KbeeValueMember value = (KbeeValueMember)sf.getCurrentSession().load(KbeeValueMember.class, this.valueId);
		return value;
	}
	
	public void setValue(DataSetMember value) {
		Assert.isInstanceOf(KbeeValueMember.class, value);
		this.valueId = value!=null ? value.getId() : null;
	}
	
	public String getDescription() {
		StringBuilder description = new StringBuilder();
		
		if (getClassifier()==null || getValue()==null) 
			return null;
		description.append(getClassifier().getDisplayName());
		description.append(" -> ");
		description.append(getValue().getDisplayName());
		return description.toString();
	}
}
