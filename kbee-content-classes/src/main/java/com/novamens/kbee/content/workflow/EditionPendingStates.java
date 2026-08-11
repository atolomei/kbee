package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.Task;

@Deprecated
public class EditionPendingStates extends KbeeStatesMachine {
	public final static String Edition_Task = "Edition";
	
	public final static String Publish_Request = "publish"; 
	public final static String Pending_Request = "pending_request"; 
	public final static String Cancel_Request = "cancel_request"; 
	public final static String Assign_Request = "assign";
	
	public Activity handle(String event, WorkflowContext context) {
		
		KbeeContext kbeecontext = (KbeeContext)context;
		Content content = kbeecontext.getContent();
		Process process = kbeecontext.getProcess(); 
		KbeeProcedure procedure = (KbeeProcedure)kbeecontext.getProcedure(); 
		
		Activity activity = null;
		Task task = null;
		
		WorkflowRule rule = getRule(event, kbeecontext);
		
		if (rule!=null) {
			rule.execute(kbeecontext);
		}
		
		switch (event) {
		case StatesMachine.Initialization_Event:
			task = procedure.getTask(Edition_Task);
			kbeecontext.setInitiator(getUser());
			task.getTrigger().pull(context, getUser());
			//content.setTitle(procedure.getName() + " " +content.getOId());
			if (procedure.getInitialRule()!=null) {
				procedure.getInitialRule().execute(kbeecontext);
			}
			break;
		case Assign_Request:
			kbeecontext.setRequester(kbeecontext.getUser());
			task = procedure.getTask(Edition_Task);
			activity = process.start(task, context, kbeecontext.getCollaborator());
			assign(content, activity.getUser(), kbeecontext.getNote());
			kbeecontext.setCollaborator(null);
			break;
		case Pending_Request:
			kbeecontext.setRequester(kbeecontext.getUser());
			task = procedure.getTask(Edition_Task);
			task.getTrigger().pull(context);
			break;
		case Cancel_Request:
			dropcheckout(content);
			process.end();
			break;
		case Publish_Request:
			checkin(content);
			process.end();
			break;
		case StatesMachine.Cancelation_Event:
			dropcheckout(content);
			break;
		default:
			Assert.isTrue(false, "inavlid event " + event);
		};
		
		return activity;
	};
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		return null;
	}
	
	public List<RoleInProcess> getRoles() {
		return new ArrayList<RoleInProcess>();
	}
	
	private void assign(Content content, User user, String note) {
		content.getService(WorkflowService.class).assign(user, note);
	}
}
