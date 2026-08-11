package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.workflow.WorkflowRule;
import com.novamens.workflow.WorkflowContext;

public class MultipleRule implements WorkflowRule, Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
	
	public MultipleRule() {
		
	}
	
	public MultipleRule(List<WorkflowRule> rules)  {
		this.rules = rules;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends WorkflowRule> List<T> getRules(Class<T> ruleClass) {
		List<T> rules = new ArrayList<>();
		for (WorkflowRule rule : this.rules) {
			if (ruleClass.isInstance(rule)) {
				rules.add((T)rule);
			}
		}
		return rules;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends WorkflowRule> T getRule(Class<T> ruleClass) {
		for (WorkflowRule rule : rules) {
			if (ruleClass.isInstance(rule)) {
				return (T)rule;
			}
		}
		return null;
	}
	
	public <T extends WorkflowRule> void setRules(List<T> rules) {
		List<WorkflowRule> newrules = new ArrayList<>();
		Class<?> ruleClass = rules.get(0).getClass();
		for (WorkflowRule rule : this.rules) {
			if (!ruleClass.isInstance(rule)) {
				newrules.add(rule);
			}
		}
		newrules.addAll(rules);
		this.rules = newrules;
	}
	
	public void execute(WorkflowContext context) {
		for (WorkflowRule rule : getRules()) {
			rule.execute(context);
		}
	}
	
	public String getDescription() {
		StringBuilder description = new StringBuilder();
		for (WorkflowRule rule : getRules()) {
			
			if (description.length()>1) 
				description.append(" | ");
			
			description.append(rule.getDescription());
		}
		return description.toString();
	}
	
	public List<WorkflowRule> getRules() {
		return rules;
	}
}
