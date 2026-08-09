package com.novamens.workflow;

public class RoutingException extends WorkflowRuntimeException {
	private static final long serialVersionUID = 1L;

	public RoutingException() {
	}
	
	public RoutingException(String message) {
		super(message);
	}
}
