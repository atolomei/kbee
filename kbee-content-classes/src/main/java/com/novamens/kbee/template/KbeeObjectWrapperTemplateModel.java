package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;

import java.util.HashSet;

public class KbeeObjectWrapperTemplateModel extends KbeeObjectTemplateModel {
	
	private Object object;
	private Set<KbeeMethod> canonicals;
	private String name;
	
	public KbeeObjectWrapperTemplateModel(Object object, String name, Set<KbeeMethod> canonicals, TemplateNodeModel parent) {
		setValues(canonicals);
		setName(name);
		setObject(object);
		setParentNode(parent);
	}
	
	public KbeeObjectWrapperTemplateModel(Object object, Set<KbeeMethod> canonicals) {
		setValues(canonicals);
		setObject(object);
	}
	
	public void setValues(Set<KbeeMethod> values) {
		this.canonicals = values;
	}
	
	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public String getAsString() throws TemplateModelException  {
		return getNodeName();
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		return name;
	}
	
	public void setName(String name)  {
		this.name = name;
	}
	
	public void setObject(Object object)  {
		this.object = object;
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "object";
	}
	
	public TemplateCollectionModel values() throws TemplateModelException {
		Set<TemplateModel> keys = new HashSet<TemplateModel>();
		return new SimpleCollection(keys, null);
	}

	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		canonicals.addAll(this.canonicals);
		return canonicals;
	}
	
	@Override
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = super.keysSet();
		return keys;
	}
}