package com.novamens.content.workflow;

public interface ScriptRule extends WorkflowRule {
	public String getScript();
	public String test(Object object);
}