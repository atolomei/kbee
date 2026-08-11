package com.novamens.kbee.content.workflow;

import com.novamens.security.User;
import com.novamens.workflow.AutomaticTrigger;
 import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeCollaboratorTrigger extends KbeeUserTrigger implements AutomaticTrigger {

	public KbeeCollaboratorTrigger() {
		setType(TriggerType.COLLABORATOR);
	}
	
	public void pull(WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		kbeecontext.setCallerTask(kbeecontext.getTask());
		kbeecontext.setRequester(kbeecontext.getUser());
		User user = ((KbeeContext)context).getCollaborator();
		if (user!=null) {
			pullToUser(context, user);
		}
		else {
			super.pull(context);
		}
	}
}
