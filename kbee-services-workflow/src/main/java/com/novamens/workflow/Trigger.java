package com.novamens.workflow;

import java.util.List;

import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;

public interface Trigger {
	public TriggerType getType();
	public void pull(WorkflowContext context);
	public void pull(WorkflowContext context, User user);
	public List<Principal> getEnabledPrincipals(WorkflowContext context);
	public List<Permission> getPermissions();
}
