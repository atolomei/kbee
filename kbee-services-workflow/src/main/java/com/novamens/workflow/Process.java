package com.novamens.workflow;

import java.time.OffsetDateTime;

import java.util.List;

import com.novamens.security.User;

public interface Process {
	
	public enum Status {
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
	
	public Long getId();
	
	public Process start();
	public boolean isRunning();
	public void cancel();
	public void end();
	
	public Activity start(Task task, WorkflowContext context, User user);
	public Activity start(Task task, WorkflowContext context, User user, boolean restart);
	public Process end(Activity activity, WorkflowEvent event, WorkflowContext context);
	
	public Procedure getProcedure();
	
	public WorkflowContext getContext();
	
	public OffsetDateTime getStartTime();
	public OffsetDateTime getEndTime();
	
	public List<Activity> getActivities();
	
	public Status getStatus();
}
