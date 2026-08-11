package com.novamens.content.workflow;

import com.novamens.workflow.WorkflowContext;

public interface WorkflowRule {
	public void execute(WorkflowContext context);
	public String getDescription();
}
