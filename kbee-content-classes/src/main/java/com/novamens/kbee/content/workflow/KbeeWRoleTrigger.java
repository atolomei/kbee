package com.novamens.kbee.content.workflow;

import com.novamens.security.User;
import com.novamens.workflow.AutomaticTrigger;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeWRoleTrigger extends KbeeUserTrigger implements AutomaticTrigger {

	public KbeeWRoleTrigger() {
		setType(TriggerType.ROLE);
	}
	
	public void pull(WorkflowContext context) {
		User user = null;
		((KbeeContext)context).setCallerTask(context.getTask());
		RoleInProcess role = getTask().getRole();
		if (role==null || context.getRoles()==null) {
			user = getLastUser(context);
		}
		else {
			user = context.getRoles().get(role);
		}	
		if (user!=null) {
			pull(context, user);
		}
		else {
			super.pull(context);
		}
	}
	

}
