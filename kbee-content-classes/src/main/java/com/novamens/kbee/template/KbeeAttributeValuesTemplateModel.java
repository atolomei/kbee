package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;

import java.util.ArrayList;
import java.util.List;

public class KbeeAttributeValuesTemplateModel extends KbeeClassificableTemplateModel implements TemplateSequenceModel {
	
	private List<String> values;
	private Attribute attribute;
	
	public KbeeAttributeValuesTemplateModel(List<String> values, Attribute attribute, TemplateNodeModel parent) {
		setValues(values);
		setAttribute(attribute);
		setParentNode(parent);
	}
	
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	public void setAttribute(Attribute value) {
		this.attribute = value;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	public TemplateModel get(int index) throws TemplateModelException {
		TemplateModel model = null;
		String value =  index<getValues().size() ? values.get(index) : null;
		model = new KbeeAttributeTemplateModel(value, getAttribute(), this);
		return model;
	}
	
	public Attribute getAttribute() {
		return attribute;
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
		return null;
	}
	
	@Override
	public List<ClassifierTemplate> getClassifiers() {
		return null;
	}
	
	@Override
	public List<AttributeTemplate> getAttributes() {
		return null;
	}
	
	@Override
	public String getAsString() {
		return getValues().toString();
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		String name = getAttribute().getAlias()!=null ? getAttribute().getAlias() : getAttribute().getDisplayName();
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