package com.novamens.workflow;

import java.util.List;

import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;

public interface UserAssignationStrategy {
	public User getUser(WorkflowContext context);
	public List<Principal> getEnabledPrincipals(WorkflowContext context);
	public List<Permission> getPermissions();
}
