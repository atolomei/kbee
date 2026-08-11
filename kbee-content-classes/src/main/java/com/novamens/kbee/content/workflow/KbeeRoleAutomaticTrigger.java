package com.novamens.kbee.content.workflow;


import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.security.acl.KbeeGroupProxy;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.workflow.Activity;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeRoleAutomaticTrigger extends KbeeUserAutomaticTrigger {
	
	public KbeeRoleAutomaticTrigger() {
		setType(TriggerType.USERAUTOMATIC_ROLE);
	}
	
	public void pull(WorkflowContext context) {
		
		User user = getLastUser(context.getProcess());

		if (user==null) {
			List<User> usersInProcess = getUsersInProcess(context);
			for (Principal principal : getEnabledUsers(context)) {
				for (User userInProcess : usersInProcess) {
					if (userInProcess.getId().equals(principal.getId())) {
						user = userInProcess;
						break;
					}
				}
				if (user!=null)
					break;
			}
		}	
		if (user!=null) {
			pull(context, user);
		}
		else {
			super.pull(context);
		}	
		
	}
	
	public List<User> getUsersInProcess(WorkflowContext context) {
		List<User> users = new ArrayList<>();
		for	(Activity activity : context.getProcess().getActivities()) {
			if (activity.getStatus().equals(Activity.Status.TERMINATED)) {
				users.add(activity.getUser());		
			}
		}	
		return users;
	}
	
	public List<Principal> getEnabledUsers(WorkflowContext context) {
		List<Principal> users = new ArrayList<>();
		for (Principal principal : getEnabledPrincipals(context)) {
			if (principal instanceof KbeeGroupProxy) {
				for (Principal member : ((KbeeGroupProxy)principal).getMembers()) {
					users.add((Principal)member);
				}
			}	
			else {
				users.add(principal);
			}
		}	
		return users;
	}
}
