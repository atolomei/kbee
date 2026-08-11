package com.novamens.content.model;

import com.novamens.beans.BeansService;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;

public abstract class ValidatorParser {
	
	public abstract Json getJson(AttributeValidator validator);
	public abstract AttributeValidator getValidator(Json json);
	
	public static ValidatorParser Get() {
		return (ValidatorParser)ServiceLocator.getService(BeansService.class).getBean("AttributeValidatorParser");
	}
}
