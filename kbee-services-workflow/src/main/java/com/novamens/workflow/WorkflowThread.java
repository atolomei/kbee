package com.novamens.workflow;

public interface WorkflowThread {
	public String getName();
	public Task getTask();
	public Procedure getProcedure();
}