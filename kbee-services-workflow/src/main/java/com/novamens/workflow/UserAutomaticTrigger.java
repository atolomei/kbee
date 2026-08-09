package com.novamens.workflow;

import com.novamens.security.User;
import com.novamens.security.acl.Permission;

public interface UserAutomaticTrigger extends AutomaticTrigger {
	public User getUser(WorkflowContext context);
	public UserAssignationStrategy getUserAssignationStrategy();
	public Permission getManualPermission();
}