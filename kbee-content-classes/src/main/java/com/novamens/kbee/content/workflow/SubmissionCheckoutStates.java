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
public class SubmissionCheckoutStates extends KbeeStatesMachine {
	
	public final static String FileReview_Task = "File Review";
	public final static String Resubmission_Task = "Resubmission";
	public final static String Collaboration_Task = "Collaboration";
	
	//public final static String Submission_Request = "submission"; 
	//public final static String Submitter_Cancel_Request = "submitter_cancel_request"; 
	//public final static String Review_Cancel_Request = "review_cancel_request"; 
	public final static String ReviewCorrection_Request = "review_correction_request";
	public final static String ReviewCorrection_Response = "review_correction_response";
	//public final static String Approve_Request = "approve_request";
	//public final static String ApproveAndPublish_Request = "approveandpublish_request";
	//public final static String Approval_Request = "approval_request";
	//public final static String Cancelation_Request = "cancelation_request"; 
	public final static String Resubmission_Request = "resubmission_request"; 
	public final static String Resubmission_Response = "resubmission_response";
	//public final static String Denegation_Request = "denegation_request"; 
	public final static String Collaboration_Request = "collaboration_request"; 
	public final static String Collaboration_Response = "return_to_editor"; 
	public final static String Publish_Request = "publish_request"; 
	//public final static String FinalSubmission_Request = "final_submission_request";
	//public final static String FinalSubmission_Response = "final_submission_response";
	
	public final static RoleInProcess Submitter_Role = new KbeeWRole("Submitter", "Submitter"); 
	public final static RoleInProcess Analyst_Role = new KbeeWRole("Analyst", "Analyst"); 
	public final static RoleInProcess Collaborator_Role = new KbeeWRole("Collaborator", "Collaborator");
	
	public Activity handle(String event, WorkflowContext context) {
		
		KbeeContext kbeecontext = (KbeeContext)context;
		
 		Content content = (Content)getContentDao().reload(kbeecontext.getContent());
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
			task = procedure.getTask(FileReview_Task);
			kbeecontext.setInitiator(getUser());
			task.getTrigger().pull(context, getUser());
			if (procedure.getInitialRule()!=null) {
				procedure.getInitialRule().execute(kbeecontext);
			}
			break;
		case Resubmission_Request:
			task = procedure.getTask(Resubmission_Task);
			task.getTrigger().pull(context);
			break;
		case Resubmission_Response:
			task = procedure.getTask(FileReview_Task);
			task.getTrigger().pull(context);
			break;
		case Collaboration_Request:
			task = procedure.getTask(Collaboration_Task);
			task.getTrigger().pull(context);
			break;
		case Collaboration_Response:
			task = kbeecontext.getCallerTask();
			task.getTrigger().pull(context, kbeecontext.getRequester());
			break;
		case Publish_Request:
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
	};
	
	public List<RoleInProcess> getRoles() {
		List<RoleInProcess> roles = new ArrayList<RoleInProcess>();
		roles.add(Submitter_Role);
		roles.add(Analyst_Role);
		roles.add(Collaborator_Role);
		return roles;
	}
}
