package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class KbeeClassificableTemplateModel  extends KbeeObjectTemplateModel {
	
	private Map<String, ClassifierTemplate> classifiersMap;
	private Map<String, AttributeTemplate> attributesMap;
	
	public KbeeClassificableTemplateModel() {
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = null;
		ClassifierTemplate classifiertemplate = getClassifier(key);
		if (classifiertemplate!=null) {
			model = getModel(classifiertemplate);
		}
		else {
			AttributeTemplate attributetemplate = getAttribute(key);
			if (attributetemplate!=null) {
				model = getModel(attributetemplate);
			}
			else {
				model = super.get(key);
			}	
		}	
		return model;
	}

	public TemplateCollectionModel values() throws TemplateModelException {
		Set<TemplateModel> keys = new HashSet<TemplateModel>();
		return new SimpleCollection(keys, null);
	}
	
	public ClassifierTemplate getClassifier(String key) {
		return getClassifiersMap().get(key.toLowerCase());
	}
	
	public AttributeTemplate getAttribute(String key) {
		return getAttributesMap().get(key.toLowerCase());
	}
	
	public abstract Classificable getClassificable();
	
	public abstract List<ClassifierTemplate> getClassifiers();
	
	public abstract List<AttributeTemplate> getAttributes();
	
	protected Map<String, ClassifierTemplate> getClassifiersMap() {
		if (classifiersMap==null) {
			classifiersMap = new HashMap<String, ClassifierTemplate>();
			for (ClassifierTemplate template : getClassifiers()) {
				Classifier classifier = template.getClassifier();
				classifiersMap.put(classifier.getAlias()!=null ? classifier.getAlias().toLowerCase() : classifier.getName().toLowerCase(), template);
			}
		}
		return classifiersMap;
	}
	
	protected Map<String, AttributeTemplate> getAttributesMap() {
		if (attributesMap==null) {
			attributesMap = new HashMap<String, AttributeTemplate>();
			for (AttributeTemplate template : getAttributes()) {
				Attribute attribute = template.getAttribute();
				attributesMap.put(attribute.getAlias()!=null ? attribute.getAlias().toLowerCase() : attribute.getName().toLowerCase(), template);
			}
		}
		return attributesMap;
	}
	
	protected List<TemplateModel> getChilds() {
		List<TemplateModel> childs = super.getChilds();
		for (AttributeTemplate template : getAttributesMap().values()) {
			TemplateModel model = getModel(template);
			if (model!=null)
			childs.add(model);
		}
		for (ClassifierTemplate template : getClassifiersMap().values()) {
			TemplateModel model = getModel(template);
			if (model!=null)
			childs.add(model);
		}
		childs.sort(new Comparator<TemplateModel>() {
			@Override
			public int compare(TemplateModel m1, TemplateModel m2) {
				try {
					if (m1 instanceof TemplateNodeModel && m2 instanceof TemplateNodeModel) {
					return ((TemplateNodeModel)m1).getNodeName().toLowerCase().compareTo(((TemplateNodeModel)m2).getNodeName().toLowerCase());
					}
					else
						return 0;
					
				} 
				catch (Exception e) {
					return 0;	
				}
			}
		});
		return childs;
	}
	
	protected TemplateModel getModel(ClassifierTemplate template) {
		TemplateModel model = null;
		if (template!=null && getClassificable()!=null) {
			List<DataSetMember> values = new ArrayList<DataSetMember>();
			for (Classification classification : getClassificable().getClassification(template.getClassifier())) {
				if (classification.getDataSetMember()!=null && template.getClassifier()!=null) {
					values.add(classification.getDataSetMember());
				}	
			};
			if (template.getMultiplicity().isMultiple()) {
				model = new KbeeValuesTemplateModel(values, template.getClassifier(), this);
			}
			else {
				DataSetMember value = values.isEmpty() ? null : values.get(0);
				if (value!=null)
				model = new KbeeValueTemplateModel(value, template.getClassifier(), this);
			}
		}
		return model;
	}
	
	protected TemplateModel getModel(AttributeTemplate template) {
		TemplateModel model = null;
		if (template!=null && getClassificable()!=null) {
			List<String> values = getClassificable().getAttributeValues(template.getAttribute());
			String value = values.isEmpty() ? null : values.get(0);
			if (value!=null)
			if (template.getAttribute().isDate()) {
				model = new KbeeDateAttributeTemplateModel(value, template.getAttribute(), this);
			}
			else {
				model = new KbeeAttributeTemplateModel(value, template.getAttribute(), this);
			}
		}
		return model;
	}

	
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = new HashSet<String>();
		keys.addAll(getClassifiersMap().keySet());
		return keys;
	}
}