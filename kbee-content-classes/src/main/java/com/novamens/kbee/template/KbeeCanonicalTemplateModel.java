package com.novamens.kbee.template;

import freemarker.template.*;

public class KbeeCanonicalTemplateModel implements TemplateNodeModel, TemplateScalarModel {
	
	private TemplateNodeModel parent;
	private String name;
	private Object value;
	public KbeeCanonicalTemplateModel(String name, Object value) {
		setName(name);
		setValue(value);
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public String getAsString() throws TemplateModelException {
		return value.toString();
	}
	
	public void setParentNode(TemplateNodeModel parent) {
		this.parent = parent;
	}
	
	public TemplateNodeModel getParentNode() throws TemplateModelException {
		return parent;
	}

	public TemplateSequenceModel getChildNodes() throws TemplateModelException {
		return null;
	}

	public void setName(String name)  {
		this.name = name;
	}
	
	public String getNodeName() throws TemplateModelException {
		return name;
	}
	
	public String getNodeType() throws TemplateModelException {
		return "Canonical";
	}
	
	public String getNodeNamespace() throws TemplateModelException {
		return null;
	}

}