package com.novamens.kbee.content.workflow;

import java.util.List;
import java.util.Map;

import com.novamens.security.Principal;
import com.novamens.workflow.Activity;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.WorkflowContext;

public interface StatesMachine {
	public static String Initialization_Event = "Initialization";
	public static String Cancelation_Event = "Cancelation";
	public Activity handle(String event, WorkflowContext context);
	public List<RoleInProcess> getRoles();
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context);
}
