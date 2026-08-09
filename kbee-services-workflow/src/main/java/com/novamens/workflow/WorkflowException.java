package com.novamens.workflow;

public class WorkflowException extends Exception {
	private static final long serialVersionUID = 1L;

	public WorkflowException() {
	}
	
	public WorkflowException(String message) {
		super(message);
	}
}
