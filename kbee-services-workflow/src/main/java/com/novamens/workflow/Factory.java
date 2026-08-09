package com.novamens.workflow;

import com.novamens.security.User;

public interface Factory {
	public Process createProcess(Procedure procedure, WorkflowContext content);
	public Activity createActivity(Task task, WorkflowContext content, User user);
}