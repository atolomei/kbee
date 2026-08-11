package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowListener;

public class TaskProxy implements Task, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Serializable procedureId;
	private String taskId;
	private transient Task task;

	public TaskProxy(Task task) {
		procedureId = ((KbeeTask)task).getProcedure().getId();
		taskId = task.getId();
	}
	
	public String getId() {
		return taskId;
	}
	
	public String getName() {
		return getTask().getName();
	}
	
	public String getDisplayName() {
		return getTask().getDisplayName();
	}
	
	public String getDescription() {
		return getTask().getDescription();
	}
	
	public boolean isReadOnly() {
		return getTask().isReadOnly();
	}
	
	public Trigger getTrigger() {
		return getTask().getTrigger();
	}
	
	public TriggerType getTriggerType() {
		return null;
	}
	
	public RoleInProcess getRole() {
		return null;
	}
	
	public ProcedurePhase getPhase() {
		return null;
	}
	
	public Activity start(WorkflowContext context, User user) {
		return null;
	}
	
	public WorkflowListener getListener() {
		return null;
	}
	
	public String getPrecondition() {
		return getTask().getPrecondition();
	}
	
	public boolean precondition(WorkflowContext context) {
		return getTask().precondition(context);
	}
	
	public boolean isInitial() {
		return getTask().isInitial();
	}
	
	public Procedure getProcedure() {
		return getTask().getProcedure();
	}
	
	public ResolutionAction getResolutionAction() {
		return null;
	}
	
	public Task getTask() {
		if (task==null) {
			Procedure procedure = getWorkflowDao().getProcedure(procedureId);
			if (procedure!=null) {
				for (Task t : procedure.getTasks()) {
					if (t.getId()!=null && t.getId().equals(taskId)) {
						task = t;
						break;
					}
				}
			}
		}
		return task;
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}
