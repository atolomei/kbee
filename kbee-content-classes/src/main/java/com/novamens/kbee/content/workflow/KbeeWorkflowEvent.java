package com.novamens.kbee.content.workflow;

import com.novamens.content.base.Content;
import com.novamens.event.AbstractEvent;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowEvent;

public class KbeeWorkflowEvent extends AbstractEvent implements WorkflowEvent {
	private Activity activity;
	private String id;
	private String label;
	private boolean forced = false;

	public KbeeWorkflowEvent(String id) {
		setId(id);
		setLabel(id);
	}
	
	public KbeeWorkflowEvent(String id, String label) {
		setId(id);
		setLabel(label);
	}
	
	public KbeeWorkflowEvent(Content content, Activity activity) {
		super(content);
		setActivity(activity);
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	protected void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String event) {
		this.id = event;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setForced(boolean value) {
		this.forced = value;
	}
	
	public boolean getForced() {
		return forced;
	}
	
	protected void setLabel(String event) {
		this.label = event;
	}
}
