package com.novamens.workflow;

public interface Router {
	public RouterType getType();
	public boolean isPublisher();
	public boolean isCanceller();
	public Task getNextTask(WorkflowContext context, String event);
}
