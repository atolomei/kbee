package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.security.acl.Permission;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;

public abstract class KbeeTrigger implements Trigger {
	private TriggerType type;
	private Task task;
	
	public TriggerType getType() {
		return type;
	}
	
	public void setType(TriggerType type) {
		this.type = type;
	}
	
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}
	
	public List<Permission> getPermissions() {
		return new ArrayList<Permission>();
	}
}
