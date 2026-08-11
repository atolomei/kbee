package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowRuntimeException;
import com.novamens.workflow.WorkflowThreadStatus.Status;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;

public class DynamicStates extends KbeeStatesMachine {
	public final static String Edition_Task = "Edition";
	public final static String EditionWithApprove_Task = "EditionWithApprove";
	public final static String Approve_Task = "Approval";
	public final static String Collaboration_Task = "Collaboration";
	
	public final static String Publish_Request = "publish"; 
	public final static String Cancel_Request = "cancel"; 
	public final static String Correction_Request = "correction_request"; 
	public final static String Collaboration_Request = "collaboration_request"; 
	public final static String Collaboration_Response = "return_to_editor"; 
	public final static String Approve_Response = "approve"; 
	public final static String Thread_End = "thread_end"; 
	
	public Activity handle(String event, WorkflowContext context) {
		
		KbeeContext kbeecontext = (KbeeContext)context;
		Content content = kbeecontext.getContent();
		Process process = kbeecontext.getProcess(); 
		KbeeProcedure procedure = (KbeeProcedure)kbeecontext.getProcedure().getMaster(); 
		
		Activity activity = null;
		Task task = null;
		
		task = kbeecontext.getTask();
		
		WorkflowRule rule = getRule(event, kbeecontext);
		
		if (rule!=null) {
			rule.execute(kbeecontext);
		}
		
		ManualEndCondition transition = getTransition(procedure, event);
		
		RouterType router = transition!=null ? transition.getRouter() : null;
		
		if (router==RouterType.RETURN_TO_CALLER) {
			event = Collaboration_Response;
		}
		else
		if (router==RouterType.PUBLISH) {
			event = Publish_Request;
		}
		
		switch (event) {
		case StatesMachine.Initialization_Event:
			task = procedure.getInitial();
			if (task == null) {
			throw new WorkflowRuntimeException("no initial");
			}

			//task = getInitialTask(procedure);
			kbeecontext.setInitiator(getUser());
			if (procedure.getInitialRule()!=null) {
				procedure.getInitialRule().execute(kbeecontext);
			}
			task.getTrigger().pull(context, getUser());
			break;
		case Publish_Request:
			checkin(content);
			process.end();
			break;
		case Collaboration_Response:
			if (transition==null) throw new WorkflowRuntimeException("Event not found");
			task = kbeecontext.getCallerTask();
			Trigger trigger = transition.getTrigger()!=null ? transition.getTrigger() : task.getTrigger(); 
			if (((KbeeTrigger)trigger).getTask()==null) ((KbeeTrigger)trigger).setTask(task);
			trigger.pull(kbeecontext);
			break;
		case Cancel_Request:
			checkin(content);
			process.end();
			break;
		case StatesMachine.Cancelation_Event:
			content.getService(ContentService.class).dropCheckout();
			break;
		case Thread_End:
			KbeeContext parentcontext = (KbeeContext)kbeecontext.getParentContext();
			KbeeWorkflowThreadStatus threadstatus = (KbeeWorkflowThreadStatus)parentcontext.getThread(kbeecontext.getThread());
			threadstatus.setStatus(Status.TERMINATED);
			threadstatus.setReason(kbeecontext.getReason());
			threadstatus.setContent(null);
			getWorkflowDao().update(parentcontext);
			getContentDao().delete(content);
			
			KbeeScriptRouter srouter = new KbeeScriptRouter();
			srouter.setScript(((KbeeForkJoinTask)parentcontext.getTask()).getRouterScript());
			task = srouter.getNextTask(parentcontext, null);
			if (task==null) {
				event = srouter.getEvent(parentcontext);
				if (event!=null) {
					switch (event) {
						case Publish_Request:
							checkin(parentcontext.getContent());
							process.end();
							break;
						default:
							
					}	
				}
			}
			else {
				trigger = task.getTrigger(); 
				if (((KbeeTrigger)trigger).getTask()==null) ((KbeeTrigger)trigger).setTask(task);
				trigger.pull(parentcontext);
			}

			
			break;

		default:
			if (transition==null) throw new WorkflowRuntimeException("Event not found");
			task = getTask(context, procedure, event);
			if (task==null) {
				event = getEvent(context, procedure, event);
				switch (event) {
					case Publish_Request:
						checkin(content);
						process.end();
						break;
					default:
						throw new WorkflowRuntimeException("wrong router info");
				}	
			}
			else {
				while (task.getPrecondition()!=null) {
					if (!precondition(task, kbeecontext)) {
						task = ((TaskProxy)task).getTask();
						task = ((KbeeTask)task).getTaskOnPreconditionFail();
					}
					else {
						break;
					}
				}
				trigger = transition.getTrigger()!=null ? transition.getTrigger() : task.getTrigger(); 
				if (((KbeeTrigger)trigger).getTask()==null) ((KbeeTrigger)trigger).setTask(task);
				trigger.pull(kbeecontext);
			}
		};
		
		return activity;
	}
	
	public List<RoleInProcess> getRoles() {
		return new ArrayList<RoleInProcess>();
	}
	
//	protected Task getInitialTask(KbeeProcedure procedure) {
//		Task initial = null;
//		for (Task task : procedure.getTasks()) {
//			if (task.isInitial()) {
//				if (initial!=null) {
//					throw new WorkflowRuntimeException("no initial");
//				}
//				initial = task;
//			}
//		}
//		if (initial == null) {
//			throw new WorkflowRuntimeException("no initial");
//		}
//		return initial;
//	}

	protected Task getTask(WorkflowContext context, KbeeProcedure procedure, String event) {
		Task nextTask = null;
		for (Task task : procedure.getTasks()) {
			if (task instanceof KbeeTask) {
				if (((KbeeTask)task).getEndConditions()!=null)
				for (EndCondition endcondtion : ((KbeeTask)task).getEndConditions()) {
					if (event.equals(endcondtion.getEvent())) {
						nextTask = endcondtion.getNextTask();
						if (((ManualEndCondition)endcondtion).getRouterScript()!=null) {
							nextTask = evaluateTask(context, (ManualEndCondition)endcondtion);
						}
					}
				}
			}
		}
		return nextTask;
	}
	
	protected String getEvent(WorkflowContext context, KbeeProcedure procedure, String event) {
		String nextevent = null;
		for (Task task : procedure.getTasks()) {
			if (task instanceof KbeeTask) {
				if ( ((KbeeTask)task).getEndConditions()!=null)
				for (EndCondition endcondtion : ((KbeeTask)task).getEndConditions()) {
					if (event.equals(endcondtion.getEvent())) {
						if (((ManualEndCondition)endcondtion).getRouterScript()!=null) {
							nextevent = evaluateEvent(context, (ManualEndCondition)endcondtion);
						}
					}
				}
			}
		}
		return nextevent;
	}
	
	protected Task evaluateTask(WorkflowContext context, ManualEndCondition condition) {
		KbeeScriptRouter router = new KbeeScriptRouter();
		router.setScript(condition.getRouterScript());
		Task task = router.getNextTask(context, null);
		return task;
	}
	
	protected String evaluateEvent(WorkflowContext context, ManualEndCondition condition) {
		KbeeScriptRouter router = new KbeeScriptRouter();
		router.setScript(condition.getRouterScript());
		String event = router.getEvent(context);
		return event;
	}

}
