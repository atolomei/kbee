package com.novamens.kbee.content.workflow;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Reason;
import com.novamens.workflow.WorkflowThread;
import com.novamens.workflow.WorkflowThreadStatus;

public class KbeeWorkflowThreadStatus implements WorkflowThreadStatus {

	Status status;
	WorkflowThread thread;
	Content content;
	Reason reason;
	
	public KbeeWorkflowThreadStatus() {
		this.status = Status.INITIAL;
	}
	
	public KbeeWorkflowThreadStatus(WorkflowThread thread, Content content) {
		this.thread = thread;
		this.content = content;
		this.status = Status.INITIAL;
	}
	
	public Status getStatus() {
		Activity activity = getContent()!=null ?
			((KbeeContext)getContent().getService(WorkflowService.class).getContext()).getCurrentActivity() :
			null;
		if (activity!=null && activity.isRunning())
			return Status.RUNNING;
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public WorkflowThread getThread() {
		return thread;
	}
	
	public void setThread(WorkflowThread thread) {
		this.thread = thread;
	}
	
	public Content getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content = content;
	}
	
	public Reason getReason() {
		return reason;
	}
	
	public void setReason(Reason reason) {
		this.reason = reason;
	}
}