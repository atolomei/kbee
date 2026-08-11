package com.novamens.kbee.content.workflow;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;

public abstract class RuleParser {
	
	public abstract Json getJson(WorkflowRule rule);
	public abstract WorkflowRule getRule(Json rule);
	
	public static RuleParser Get() {
		return (RuleParser)ServiceLocator.getService(BeansService.class).getBean("WorkflowRuleParser");
	}
}
