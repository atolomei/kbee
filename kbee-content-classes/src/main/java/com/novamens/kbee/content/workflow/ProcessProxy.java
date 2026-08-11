package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.workflow.WorkflowDao;
import com.novamens.security.User;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowEvent;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;

public class ProcessProxy implements Process {

	private Procedure procedure;
	private Process process;
	private WorkflowContext context;
	private WorkflowDao dao;
	private Long id;
	
	public ProcessProxy(WorkflowDao dao) {
		this.dao = dao;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Process start() {
		return this;
	}
	
	public Activity start(Task task, WorkflowContext context, User user) {
		return getProcess().start(task, context, user); 
	}
	
	public Activity start(Task task, WorkflowContext context, User user, boolean restart) {
		return getProcess().start(task, context, user, restart); 
	}
	
	public void cancel() {
		getProcess().cancel(); 
	}
	
	public void end() {
		getProcess().end(); 
	}
	
	public boolean isRunning() {
		return getProcess().isRunning();
	}
	
	public Process end(Activity activity, WorkflowEvent event, WorkflowContext context) {
		getProcess().end(activity, event, context);
		setContext(getProcess().getContext());
		return this;
	}
	
	public WorkflowContext getContext() {
		return context;
	}
	
	public void setContext(WorkflowContext context) {
		((KbeeContext)context).setProcess(this);
		this.context = context;
	}
	
	// no deberia modificar el contexto si el proceso ya lo tiene
	public Procedure getProcedure() {
		if (process==null) {
			process = dao.findProcessById(getId());
			procedure = process.getProcedure();
			if (process!=null)
			((KbeeProcess)process).setContext(getContext());
		}			
		return procedure;
	}
	
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}
	
	public  OffsetDateTime getStartTime() {
		return getProcess().getStartTime(); 
	}
	
	public OffsetDateTime getEndTime() {
		return null;
	}
	
	public Process getProcess() {
		if (process==null) {
			process = dao.findProcessById(getId());
			procedure = process.getProcedure();
			if (process!=null)
			((KbeeProcess)process).setContext(getContext());
		}			
		return process;
	}
	
	public List<Activity> getActivities() {
		return getProcess().getActivities(); 
	}
	
	public Status getStatus() {
		return getProcess().getStatus(); 
	}
	
	public void detach() {
		process =  null;
		procedure = null;
	}
}
