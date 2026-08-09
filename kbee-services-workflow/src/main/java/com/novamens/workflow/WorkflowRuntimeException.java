package com.novamens.workflow;

public class WorkflowRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public WorkflowRuntimeException() {
	}
	
	public WorkflowRuntimeException(String message) {
		super(message);
	}
}
