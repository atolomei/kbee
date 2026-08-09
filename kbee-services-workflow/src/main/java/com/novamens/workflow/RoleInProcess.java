package com.novamens.workflow;

import com.novamens.security.Identifiable;

/**
 * 
 *  *<p> A Workflow Role is a role specific for the Business Process. 
 *  Every Tasks has one and only one Workflow-Profile. A Workflow-Profile can be seen as a tag applied to the user that executes the Task. 
 *  When the Task starts, the user that executes the Task is assigned the Task's Workflow-Profile. This information is used by the workflow engine for routing, 
 *  next time it needs to assign a task to the workflow role it will look for the user that has the 'tag attached'.</p>

 *
 */
public interface RoleInProcess extends Identifiable {
	public String getName();
	public String getLabel();
}
