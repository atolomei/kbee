package com.novamens.content.workflow;

import com.novamens.workflow.WorkflowContext;

public interface Validator {
	public boolean validate(WorkflowContext context);
	public String getMessage();
}
