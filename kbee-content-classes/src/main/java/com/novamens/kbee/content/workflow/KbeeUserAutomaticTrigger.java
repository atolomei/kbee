package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.UserAssignationStrategy;
import com.novamens.workflow.UserAutomaticTrigger;
import com.novamens.workflow.WorkflowContext;

public class KbeeUserAutomaticTrigger extends KbeeUserTrigger implements UserAutomaticTrigger{
	private UserAssignationStrategy assignationStrategy;
	
	public KbeeUserAutomaticTrigger() {
		setType(TriggerType.USERAUTOMATIC);
	}
	
	public void pull(WorkflowContext context) {
		User user = getUser(context);
		if (user!=null) {
			pullToUser (context, user);
		}
		else {
			setAsPending(getTask(), ((KbeeContext)context).getContent(), context);
		}
	}
	
	public List<Principal> getEnabledPrincipals(WorkflowContext context) {
		return getUserAssignationStrategy().getEnabledPrincipals(context);
	}
	
	@Override
	public List<Permission> getPermissions() {
		ArrayList<Permission> permissions = new ArrayList<Permission>();
		permissions.addAll(getUserAssignationStrategy().getPermissions());
		permissions.add(getManualPermission());
		return permissions;
	}
	
	public User getUser(WorkflowContext context) {
		return getUserAssignationStrategy().getUser(context);
	}
	
	public UserAssignationStrategy getUserAssignationStrategy() {
		return assignationStrategy;
	}
	
	public void setUserAssignationStrategy(UserAssignationStrategy strategy) {
		this.assignationStrategy = strategy;;
	}
}