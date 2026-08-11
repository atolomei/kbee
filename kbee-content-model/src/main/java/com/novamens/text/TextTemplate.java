package com.novamens.text;

public interface TextTemplate {
	//public void process(Object model, Writer out) throws TemplateException;
	public String process(Object model) throws TemplateException;
}
