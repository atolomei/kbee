package com.novamens.kbee.template;

import freemarker.template.*;
import java.util.Set;


import java.util.HashSet;
import java.util.List;

public class KbeeListWrapperTemplateModel extends KbeeObjectTemplateModel implements TemplateSequenceModel {
	
	private List<Object> objects;
	private Set<KbeeMethod> canonicals;
	private String name;
	
	public KbeeListWrapperTemplateModel(List<Object> values, String name, Set<KbeeMethod> canonicals, TemplateNodeModel parent) {
		setValues(canonicals);
		setName(name);
		setObjects(values);
		setParentNode(parent);
	}
	
	public void setValues(Set<KbeeMethod> values) {
		this.canonicals = values;
	}
	
	public void setObjects(List<Object> objects)  {
		this.objects = objects;
	}
	
	public List<Object> getObjects() {
		return objects;
	}
	
	public TemplateModel get(int index) throws TemplateModelException {
		TemplateModel model = null;
		Object value =  index<getObjects().size() ? getObjects().get(index) : null;
		model = wrap(new KbeeMethod(getNodeName()),value);
		return model;
	}
	
	public TemplateCollectionModel values() throws TemplateModelException {
		Set<TemplateModel> keys = new HashSet<TemplateModel>();
		return new SimpleCollection(keys, null);
	} 
	
	public int size() throws TemplateModelException {
		return getObjects().size();
	}
	
	@Override
	public Object getObject() {
		return !getObjects().isEmpty() ? getObjects().get(0) : null;
	}
	
	@Override
	public String getAsString() {
		return this.getClass().getName();
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		return name + "[]";
	}
	
	public void setName(String name)  {
		this.name = name;
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "objects";
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