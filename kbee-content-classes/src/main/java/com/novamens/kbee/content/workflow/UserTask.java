package com.novamens.kbee.content.workflow;

import java.util.List;

import com.novamens.content.form.EForm;
import com.novamens.content.workflow.EndCondition;
import com.novamens.workflow.Task;

public interface UserTask extends Task  {
	public List<EndCondition> getEndConditions();
	public List<EForm> getForms();
	public boolean isProgressNotesEnabled();
}
