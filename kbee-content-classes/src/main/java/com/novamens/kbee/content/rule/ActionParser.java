package com.novamens.kbee.content.rule;

import com.novamens.beans.BeansService;
import com.novamens.content.rule.Action;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;

public abstract class ActionParser {
	
	public abstract Json getJson(Action action);
	public abstract Action getAction(Json json);
	
	public static ActionParser Get() {
		return (ActionParser)ServiceLocator.getService(BeansService.class).getBean("RuleActionParser");
	}
}
