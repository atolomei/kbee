package com.novamens.text;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public abstract class TemplateModelParser {
	
	public abstract String getJson(TemplateModelInfo model);
	public abstract TemplateModelInfo getModel(String json);
	
	public static TemplateModelParser Get() {
		return (TemplateModelParser)ServiceLocator.getService(BeansService.class).getBean("TemplateModelParser");
	}
}