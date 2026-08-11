package com.novamens.kbee.template;

import com.novamens.content.base.Content;
import com.novamens.content.service.UrlService;

import freemarker.template.*;

public class KbeeServiceTemplateModel implements TemplateNodeModel, TemplateScalarModel {
	
	private TemplateNodeModel parent;
	private String name;
	private Content content;
	
	public KbeeServiceTemplateModel(String name, Content content) {
		setName(name);
		this.content = content;
	}
	
	public String getValue() {
		if ("publicUrl".equals(name)) {
			return getContent().getService(UrlService.class).getPublicUrl();
		}
		return null;
	}
	
	public String getAsString() throws TemplateModelException {
		return getValue();
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
		return "Service";
	}
	
	public String getNodeNamespace() throws TemplateModelException {
		return null;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
}