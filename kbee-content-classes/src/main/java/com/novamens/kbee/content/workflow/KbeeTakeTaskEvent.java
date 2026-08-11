package com.novamens.kbee.content.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.event.AbstractEvent;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowEvent;

public class KbeeTakeTaskEvent extends AbstractEvent implements WorkflowEvent {

	private Task task;
	private AjaxRequestTarget target;

	public KbeeTakeTaskEvent(Task task) {
		setTask(task);
	}
	
	public KbeeTakeTaskEvent(Task task, AjaxRequestTarget target) {
		setTask(task);
		this.target=target;
		
	}
	

	public AjaxRequestTarget getTarget() {
		return this.target;
	}
	
	
	public String getId() {
		return null;
	}
	
	public String getLabel() {
		return null;
	}
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Activity getActivity() {
		return null;
	}
	
	
	public boolean getForced() {
		return false;
	}
}
