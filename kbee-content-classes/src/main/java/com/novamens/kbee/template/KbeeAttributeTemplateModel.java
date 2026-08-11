package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import com.novamens.content.model.Attribute;

import java.util.HashSet;

public class KbeeAttributeTemplateModel extends KbeeObjectTemplateModel  {
	
	private String value;
	private Attribute attribute;
	
	public KbeeAttributeTemplateModel(String value, Attribute attribute, TemplateNodeModel parent) {
		setValue(value);
		setAttribute(attribute);
		setParentNode(parent);
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void setAttribute(Attribute value) {
		this.attribute = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
 	
	@Override
	public Object getObject() {
		return value;
	}
	
	@Override
	public String getAsString() {
		return getValue();
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		return getAttribute().getAlias()!=null ? getAttribute().getAlias() : getAttribute().getDisplayName(); 
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "attribute";
	}
	
	public TemplateCollectionModel values() throws TemplateModelException {
		Set<TemplateModel> keys = new HashSet<TemplateModel>();
		return new SimpleCollection(keys, null);
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
