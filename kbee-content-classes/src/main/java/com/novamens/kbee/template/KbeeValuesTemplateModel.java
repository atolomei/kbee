package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;

import java.util.ArrayList;
import java.util.List;

public class KbeeValuesTemplateModel extends KbeeClassificableTemplateModel implements TemplateSequenceModel {
	
	private List<DataSetMember> values;
	private Classifier classifier;
	
	public KbeeValuesTemplateModel(List<DataSetMember> values, Classifier classifier, TemplateNodeModel parent) {
		setValues(values);
		setClassifier(classifier);
		setParentNode(parent);
	}
	
	public void setValues(List<DataSetMember> values) {
		this.values = values;
	}
	
	public void setClassifier(Classifier value) {
		this.classifier = value;
	}
	
	public List<DataSetMember> getValues() {
		return values;
	}
	
	public TemplateModel get(int index) throws TemplateModelException {
		TemplateModel model = null;
		DataSetMember value =  index<getValues().size() ? values.get(index) : null;
		model = new KbeeValueTemplateModel(value, getClassifier(), this);
		return model;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public int size() throws TemplateModelException {
		return getValues().size();
	}
	
	@Override
	public Object getObject() {
		return !getValues().isEmpty() ? getValues().get(0) : null;
	}
	
	@Override
	public Classificable getClassificable() {
		return !getValues().isEmpty() ? getValues().get(0) : null;
	}
	
	@Override
	public List<ClassifierTemplate> getClassifiers() {
		List<ClassifierTemplate> templates = new ArrayList<ClassifierTemplate>();
		for (ModelElementTemplate template : getClassifier().getDataSet().getStructure()) {
			if (template instanceof ClassifierTemplate && ((ClassifierTemplate)template).getClassifier()!=null) {
				templates.add((ClassifierTemplate)template);
			}
		}
		return templates;
	}
	
	@Override
	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> templates = new ArrayList<AttributeTemplate>();
		for (ModelElementTemplate template : getClassifier().getDataSet().getStructure()) {
			if (template instanceof AttributeTemplate && ((AttributeTemplate)template).getAttribute()!=null) {
				templates.add((AttributeTemplate)template);
			}
		}
		return templates;
	}
	
	@Override
	public String getAsString() {
		return !getValues().isEmpty() ? getValues().get(0).getDisplayName() : null;
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		String name = getClassifier().getAlias()!=null ? getClassifier().getAlias() : getClassifier().getDisplayName();
		name += "[]";
		return name;
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "values";
	}

	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		return canonicals;
	}
	
	@Override
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = super.keysSet();
		return keys;
	}
}