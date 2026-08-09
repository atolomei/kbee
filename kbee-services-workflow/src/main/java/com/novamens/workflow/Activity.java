package com.novamens.workflow;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.Group;


/**
 * <p>An <b> Activity</b> is a {@code Task} in execution or executed<p>  
 * 
 *
 */
public interface Activity extends Identifiable {
	
	public enum Status {
		RUNNING ("RUNNING"), 
		TERMINATED ("TERMINATED"), 
		REASSIGNED ("REASSIGNED"), 
		CANCELED ("CANCELED");
		
		private String value;
		
		private Status(String value) {
			this.value = value;
		}
		public String toString() {
			return value;
		}
	}
	
	public Task getTask();
	
	public void end();
	public void cancel();
	public void assign(User user);
	
	public Process getProcess();
	public User getUser();
	public WorkflowContext getContext();
	
	public OffsetDateTime getStartTime();
	public OffsetDateTime getEndTime();
	public OffsetDateTime getDueDate();

	
	public Status getStatus();
	public boolean isRunning();
	
	public String getEvent();
	public String getNote();
	public String getResolution();
	public String getResolutionTitle();
	
	public String getThread();
	
	public User getAssignedBy();
	
	public List<Group> getEnabledGroups();
	
	public List<ActivityProgressNote> getProgressNotes();
}