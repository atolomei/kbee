package com.novamens.kbee.content.model;

import com.novamens.beans.BeansService;
import com.novamens.content.model.ExtractionRule;
import com.novamens.service.ServiceLocator;

public abstract class ExtractionRuleParser {
	
	public abstract String getJson(ExtractionRule rule);
	public abstract ExtractionRule getRule(String json);
	
	public static ExtractionRuleParser Get() {
		return (ExtractionRuleParser)ServiceLocator.getService(BeansService.class).getBean("ExtractionRuleParser");
	}
}