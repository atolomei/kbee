package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.service.DomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Process;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;

public class KbeeAutomaticTrigger extends KbeeTrigger {
	
	public KbeeAutomaticTrigger() {
		setType(TriggerType.AUTOMATIC);
	}
	
	public void pull(WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		kbeecontext.setTask(getTask());
		Content content = kbeecontext.getContent();
		Domain domain = content.getDomain();
		User user = domain.getService(DomainService.class).getWorkflowUser();
		kbeecontext.setTask(getTask());
		Process process = kbeecontext.getProcess();
		Activity activity = process.start(getTask(), context, user);
		assign(content, activity.getUser(), context.getNote());		
	}
	
	public void pull(WorkflowContext context, User user) {
		
	}
	
	public List<Principal> getEnabledPrincipals(WorkflowContext context) {
		return new ArrayList<Principal>();
	}
	
	protected void assign(Content content, User user, String note) {
		content.getService(WorkflowService.class).assign(user, note);
	}
}