package com.novamens.kbee.content.form;

import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.form.EFormComponent;
import com.novamens.service.ServiceLocator;

public abstract class EFormParser {
	
	public abstract String getJson(List<EFormComponent> components);
	public abstract List<EFormComponent> getComponents(String json);
	
	public static EFormParser Get() {
		return (EFormParser)ServiceLocator.getService(BeansService.class).getBean("EFormComponentParser");
	}
}
