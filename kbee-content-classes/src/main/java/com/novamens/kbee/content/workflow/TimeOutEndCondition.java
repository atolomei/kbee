package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.workflow.Reason;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

public class TimeOutEndCondition implements EndCondition, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String label;
	private String event;
	private String description;
	private WorkflowRule rule;
	private int duration;
	private String note;
	private long autoRunAfter = 0;
	private Task nextTask;
	private boolean enabled = true;

	public TimeOutEndCondition(String label, String event) {
		setLabel(label);
		setEvent(event);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNote() {
		return note;
	}
	
	public void setNote(String label) {
		this.note = label;
	}
	
	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public boolean isManual() {
		return false;
	}
	
	public boolean isInfrequent() {
		return false;
	}
	
	public boolean isDefault() {
		return false;
	}
	
	public WorkflowRule getRule() {
		return rule;
	}
	
	public void setRule(WorkflowRule rule) {
		this.rule = rule;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isEnabled(Content content) {
		return enabled;
	}
	
	public void setEnabled(boolean value) {
		enabled = value;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public OffsetDateTime getDueDate(WorkflowContext context) {
		return null;
	}
	
	public List<Reason> getReasons() {
		return null;
	}
	
	public void setNextTask(Task id) {
		nextTask = id;
	}
	
	public Task getNextTask() {
		return nextTask;
	}
	
	public long getAutoRunAfter() {
		return autoRunAfter;
	}

	public void setAutoRunAfter(long autoRunAfter) {
		this.autoRunAfter = autoRunAfter;
	}
	
	public boolean isTimeout(WorkflowContext context) {
		return false;
	}

}
