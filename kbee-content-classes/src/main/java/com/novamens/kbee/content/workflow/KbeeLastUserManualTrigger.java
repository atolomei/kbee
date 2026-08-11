package com.novamens.kbee.content.workflow;

import com.novamens.security.User;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeLastUserManualTrigger extends KbeeManualTrigger {
	
	public KbeeLastUserManualTrigger() {
		setType(TriggerType.MANUAL_LASTUSER);
	}
	
	public void pull(WorkflowContext context) {
		((KbeeContext)context).setCallerTask(context.getTask());
		User user = getLastUser(context.getProcess());
		if (user!=null) {
			pull(context, user);
		}
		else {
			super.pull(context);
		}	
	}
}