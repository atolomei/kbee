package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.Task;
import com.novamens.workflow.UserAutomaticTrigger;

@Deprecated
public class EditionWithApprovalStates implements StatesMachine {
	public final static String Edition_Task = "Edition";
	public final static String EditionWithApprove_Task = "EditionWithApprove";
	public final static String Approve_Task = "Approval";
	public final static String Collaboration_Task = "Collaboration";
	
	public final static String Publish_Request = "publish"; 
	public final static String Approve_Request = "approval_request"; 
	public final static String Correction_Request = "correction_request"; 
	public final static String Collaboration_Request = "collaboration_request"; 
	public final static String Collaboration_Response = "return_to_editor"; 
	public final static String Approve_Response = "approve"; 
	
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
			activity = process.start(task, context, getUser());
			kbeecontext.setRequester(activity.getUser());
			kbeecontext.setInitiator(activity.getUser());
			if (procedure.getInitialRule()!=null) {
				procedure.getInitialRule().execute(kbeecontext);
			}
			break;
		case Correction_Request:
			task = procedure.getTask(Edition_Task);
			activity = process.start(task, context, kbeecontext.getRequester());
			assign(content, activity.getUser(), kbeecontext.getNote());
			break;
		case Collaboration_Request:
			kbeecontext.setRequester(kbeecontext.getUser());
			task = procedure.getTask(Collaboration_Task);
			activity = process.start(task, context, kbeecontext.getCollaborator());
			assign(content, activity.getUser(), kbeecontext.getNote());
			break;
		case Collaboration_Response:
			task = procedure.getTask(Edition_Task);
			activity = process.start(task, context, kbeecontext.getRequester());
			assign(content, activity.getUser(), kbeecontext.getNote());
			break;
		case Approve_Request:
			task = procedure.getTask(Approve_Task);
			kbeecontext.setRequester(kbeecontext.getUser());
			kbeecontext.setTask(task);
			User user = null;
			if (task.getTrigger()!=null) {
				if (task.getTrigger() instanceof UserAutomaticTrigger) {
					user = ((UserAutomaticTrigger)task.getTrigger()).getUser(kbeecontext);
					if (user!=null) {
						activity = process.start(task, context, user);
						assign(content, activity.getUser(), kbeecontext.getNote());
					}
				}
			}
			if (user==null) {
				setAsPending(task, content, context);
			}	
			break;
		case Publish_Request:
		case Approve_Response:
			checkin(content);
			process.end();
			break;
		case StatesMachine.Cancelation_Event:
			content.getService(ContentService.class).dropCheckout();
			break;
		default:
			Assert.isTrue(false, "inavlid event " + event);
		};
		
		return activity;
	}
	
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		return null;
	}
	
	public List<RoleInProcess> getRoles() {
		return new ArrayList<RoleInProcess>();
	}
	
	private void assign(Content content, User user, String note) {
		content.getService(WorkflowService.class).assign(user, note);
	}
	
	private void setAsPending(Task task, Content content, WorkflowContext context) {
		content.getService(WorkflowService.class).setAsPending(task, context);
	}
	

	
	private void checkin(Content content) {
		content.getService(ContentService.class).checkin();
	}

	private WorkflowRule getRule(String event, WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		
		Task actualtask  = kbeecontext.getTask();
		if (actualtask!=null) {
			for (com.novamens.content.workflow.EndCondition condition : ((KbeeTask)actualtask).getEndConditions()) {
				if (event.equals(condition.getEvent())) {
					return condition.getRule();
				}
			}
		}
		
		return null;
	}
	
	private User getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}

}
