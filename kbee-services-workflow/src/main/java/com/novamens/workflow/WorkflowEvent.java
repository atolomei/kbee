package com.novamens.workflow;

import com.novamens.event.Event;

public interface WorkflowEvent extends Event {
	public Activity getActivity();
	public String getLabel();
	public String getId();
	public boolean getForced();
}
