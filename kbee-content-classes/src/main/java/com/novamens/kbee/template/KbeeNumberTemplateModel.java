package com.novamens.kbee.template;

import freemarker.template.*;


public class KbeeNumberTemplateModel extends KbeeCanonicalTemplateModel implements TemplateNodeModel, TemplateScalarModel, TemplateNumberModel {
	
	Number value;
	
	public KbeeNumberTemplateModel(String name, Number value, TemplateNodeModel parent) {
		super(name, String.valueOf(value));
		this.value=value;
	}
	
	
	public Number getAsNumber() throws TemplateModelException {
		return value;
	}
}
