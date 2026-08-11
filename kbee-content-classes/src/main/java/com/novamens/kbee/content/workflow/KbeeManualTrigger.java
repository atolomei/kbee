package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.security.Principal;
import com.novamens.security.acl.Permission;
import com.novamens.workflow.ManualTrigger;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeManualTrigger extends KbeeUserTrigger implements ManualTrigger {

	public KbeeManualTrigger() {
		setType(TriggerType.MANUAL);
	}
	
	@Override
	public List<Principal> getEnabledPrincipals(WorkflowContext context) {
		return getEnabledPrincipals(((KbeeContext)context).getContent(), getManualPermission());
	}
	
	@Override
	public List<Permission> getPermissions() {
		ArrayList<Permission> permissions = new ArrayList<Permission>();
		permissions.add(getManualPermission());
		return permissions;
	}
	
	@Override
	public void pull(WorkflowContext context) {
		((KbeeContext)context).setCallerTask(context.getTask());
		((KbeeContext)context).setTask(getTask());
		setAsPending(getTask(), ((KbeeContext)context).getContent(), context);
	}
}
