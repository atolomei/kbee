package com.novamens.kbee.content.workflow;

import com.novamens.security.User;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeLastUserAutomaticTrigger extends KbeeUserAutomaticTrigger {
	
	public KbeeLastUserAutomaticTrigger() {
		setType(TriggerType.USERAUTOMATIC_LASTUSER);
	}
	
	public void pull(WorkflowContext context) {
		User user = getLastUser(context.getProcess());
		if (user!=null) {
			pull(context, user);
		}
		else {
			super.pull(context);
		}	
	}
}
