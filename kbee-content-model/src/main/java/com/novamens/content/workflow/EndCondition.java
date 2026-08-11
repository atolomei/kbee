package com.novamens.content.workflow;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.workflow.Reason;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

public interface EndCondition {
	public String getLabel();
	public String getEvent();
	public String getDescription();
	
	public boolean isManual();
	public boolean isInfrequent();
	public boolean isDefault();
	public boolean isEnabled(Content content);
	
	public OffsetDateTime getDueDate(WorkflowContext context);
	
	public long getAutoRunAfter();
	public boolean isTimeout(WorkflowContext context);
	
	public WorkflowRule getRule();
	public List<Reason> getReasons();
	public Task getNextTask();
}