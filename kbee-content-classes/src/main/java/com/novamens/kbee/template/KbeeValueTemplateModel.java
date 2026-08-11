package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;

import java.util.ArrayList;
import java.util.List;

public class KbeeValueTemplateModel extends KbeeClassificableTemplateModel {
	
	private DataSetMember value;
	private Classifier classifier;
	
	public KbeeValueTemplateModel(DataSetMember value) {
		setValue(value);
	}
	
	public KbeeValueTemplateModel(DataSetMember value, Classifier classifier, TemplateNodeModel parent) {
		setValue(value);
		setClassifier(classifier);
		setParentNode(parent);
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = super.get(key);
		if (model==null && key!=null && "path".equals(key.toLowerCase())) {
			model = new KbeePathModel();
		}
		if (model==null && key!=null && "shortpath".equals(key.toLowerCase())) {
			model = new KbeeShortPathModel();
		}
		return model;
	}
	
	public void setValue(DataSetMember value) {
		this.value = value;
	}
	
	public void setClassifier(Classifier value) {
		this.classifier = value;
	}
	
	public DataSetMember getValue() {
		return value;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public DataSet getDataSet() {
		return getClassifier()!=null ? getClassifier().getDataSet() : (getValue()!=null ? getValue().getDataSet() : null);
	}
	
	@Override
	public Object getObject() {
		return value;
	}
	
	@Override
	public Classificable getClassificable() {
		return value;
	}
	
	@Override
	public List<ClassifierTemplate> getClassifiers() {
		List<ClassifierTemplate> templates = new ArrayList<ClassifierTemplate>();
		for (ModelElementTemplate template : getDataSet().getStructure()) {
			if (template instanceof ClassifierTemplate && ((ClassifierTemplate)template).getClassifier()!=null) {
				templates.add((ClassifierTemplate)template);
			}
		}
		return templates;
	}
	
	@Override
	public List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> templates = new ArrayList<AttributeTemplate>();
		for (ModelElementTemplate template : getDataSet().getStructure()) {
			if (template instanceof AttributeTemplate && ((AttributeTemplate)template).getAttribute()!=null) {
				templates.add((AttributeTemplate)template);
			}
		}
		return templates;
	}
	
	@Override
	public String getAsString() {
		return getValue().getDisplayName();
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		return getClassifier()!=null 
			? (getClassifier().getAlias()!=null ? getClassifier().getAlias() : getClassifier().getDisplayName())
			: getNodeType(); 
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "value";
	}

	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		canonicals.add(new KbeeMethod("Parents", "Parents"));
		canonicals.add(new KbeeMethod("Value", "Value"));
		return canonicals;
	}
	
	@Override
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = super.keysSet();
		return keys;
	}
}
