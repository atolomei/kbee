package com.novamens.workflow;

import com.novamens.security.Identifiable;
import com.novamens.security.User;

/**
 * 

 NAME  Name(initial) -> the initial task when the Workflow starts.

SECURITY TRIGGER 
Task starting strategy. Whether the new task is sent to Pending or assigned to last user who did the same task before, or sent to user with right Role ...). Initial Task is special case, the trigger is manual

ROUND ROBIN  
Assign the task to the user/s that have the Workflow-Profile assigned via Security Roles. If there is more than one user the assign policy is round-robin.

OTHER
There are more triggers available see here.

ACTION
Action that terminates the Task -> routing strategy for the action

Workflow-Profile
Workflow Role to assign to the user that executes the Task

PRECONDITION
IQL Condition that must be true for the Task to Start. If it is not true, the Task is sent to Pending

 *
 */
public interface Task extends Identifiable {

	public String getId();
	public String getName();
	public String getDisplayName();
	public String getDescription();
	public boolean isReadOnly();
	
	public Trigger getTrigger();
	public TriggerType getTriggerType();
	
	public RoleInProcess getRole();
	public ProcedurePhase getPhase();
	
	public Activity start(WorkflowContext context, User user);
	public WorkflowListener getListener();
	
	public String getPrecondition();
	public boolean precondition(WorkflowContext context);
	
	public boolean isInitial();
	
	public ResolutionAction getResolutionAction();
	
	public Procedure getProcedure();
}