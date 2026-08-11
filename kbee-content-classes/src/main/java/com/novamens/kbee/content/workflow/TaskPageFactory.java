package com.novamens.kbee.content.workflow;

import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;


public interface TaskPageFactory {
	public TaskPage<?> getPage(Task task, WorkflowContext context);
	public String getName();
	public String getDisplayName();
}
