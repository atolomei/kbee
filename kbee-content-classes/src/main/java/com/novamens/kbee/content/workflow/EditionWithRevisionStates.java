package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;


import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.Task;

@Deprecated
public class EditionWithRevisionStates extends KbeeStatesMachine {
	public final static String Edition_Task = "Edition";
	public final static String Revision1_Task = "Revision1";
	public final static String Revision2_Task = "Revision2";
	public final static String Revision3_Task = "Revision3";
	public final static String Approve_Task = "Approval";
	public final static String Collaboration_Task = "Collaboration";
	
	public final static String Publish_Request = "publish"; 
	public final static String Revision1_Request = "revision1_request"; 
	public final static String Revision2_Request = "revision1_approve_request"; 
	public final static String Revision3_Request = "revision2_approve_request"; 
	public final static String Revision3_Approve_Request = "revision3_approve_request"; 
	public final static String Approve_Request = "approval_request"; 
	public final static String Correction_Request = "correction_request"; 
	public final static String Revision1_Correction_Request = "revision1_correction_request"; 
	public final static String Revision2_Correction_Request = "revision2_correction_request"; 
	public final static String Revision3_Correction_Request = "revision3_correction_request"; 
	public final static String Collaboration_Request = "collaboration_request"; 
	public final static String Collaboration_Response = "return_to_editor"; 
	public final static String Approve_Response = "approve";
	
	public final static RoleInProcess Editor_Role = new KbeeWRole("Editor", "Editor"); 
	public final static RoleInProcess Reviser1_Role = new KbeeWRole("Reviser1", "Reviser 1");
	public final static RoleInProcess Reviser2_Role = new KbeeWRole("Reviser2", "Reviser 2");
	public final static RoleInProcess Reviser3_Role = new KbeeWRole("Reviser3", "Reviser 3");
	public final static RoleInProcess Collaborator_Role = new KbeeWRole("Collaborator", "Collaborator");
	public final static RoleInProcess Approver_Role = new KbeeWRole("Approver", "Approver");
	
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
			task.getTrigger().pull(kbeecontext, getUser());
			if (procedure.getInitialRule()!=null) {
				procedure.getInitialRule().execute(kbeecontext);
			}
			break;
		case Revision1_Request:
			task = procedure.getTask(Revision1_Task);
			if (!precondition(task, kbeecontext)) {
				task = procedure.getTask(Revision2_Task);
				if (!precondition(task, kbeecontext)) {
					task = procedure.getTask(Revision3_Task);
					if (!precondition(task, kbeecontext)) {
						task = procedure.getTask(Approve_Task);
					}
				}
			}
			task.getTrigger().pull(kbeecontext);
			break;
		case Revision2_Request:
			task = procedure.getTask(Revision2_Task);
			if (!precondition(task, kbeecontext)) {
				task = procedure.getTask(Revision3_Task);
				if (!precondition(task, kbeecontext)) {
					task = procedure.getTask(Approve_Task);
				}
			}
			task.getTrigger().pull(kbeecontext);
			break;
		case Revision3_Request:
			task = procedure.getTask(Revision3_Task);
			if (!precondition(task, kbeecontext)) {
				task = procedure.getTask(Approve_Task);
			}
			task.getTrigger().pull(kbeecontext);
			break;
		case Correction_Request:
		case Revision1_Correction_Request:
		case Revision2_Correction_Request:
		case Revision3_Correction_Request:
			task = procedure.getTask(Edition_Task);
			task.getTrigger().pull(kbeecontext);
			break;
		case Collaboration_Request:
			task = procedure.getTask(Collaboration_Task);
			task.getTrigger().pull(kbeecontext);
			break;
		case Collaboration_Response:
			task = kbeecontext.getCallerTask();
			task.getTrigger().pull(kbeecontext);
			break;
		case Approve_Request:
		case Revision3_Approve_Request:
			task = procedure.getTask(Approve_Task);
			task.getTrigger().pull(kbeecontext);
			break;
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
	
//	public Map<Role, List<Principal>> getRoles(WorkflowContext context) {
//		return null;
//	}
	
	public List<RoleInProcess> getRoles() {
		List<RoleInProcess> roles = new ArrayList<RoleInProcess>();
		roles.add(Editor_Role);
		roles.add(Reviser1_Role);
		roles.add(Reviser2_Role);
		roles.add(Reviser3_Role);
		roles.add(Approver_Role);
		roles.add(Collaborator_Role);
		return roles;
	}
	

}
