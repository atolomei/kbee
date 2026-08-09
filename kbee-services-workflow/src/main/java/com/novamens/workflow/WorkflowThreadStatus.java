package com.novamens.workflow;

public interface WorkflowThreadStatus {
	public enum Status {
		INITIAL ("INITIAL"), 
		RUNNING ("RUNNING"), 
		TERMINATED ("TERMINATED"), 
		CANCELED ("CANCELED");
		
		private String value;
		
		private Status(String value) {
			this.value = value;
		}
		
		public String toString() {
			return value;
		}
	}
	
	public Status getStatus();
	public WorkflowThread getThread();
	public Reason getReason();
}
