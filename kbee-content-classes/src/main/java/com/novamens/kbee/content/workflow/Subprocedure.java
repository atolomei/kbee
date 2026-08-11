package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.security.Principal;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

public class Subprocedure implements ContentProcedure, DomainObject, com.novamens.content.workflow.KbeeProcedure, com.novamens.workflow.Subprocedure  {
	
	private Procedure procedure;
	private Serializable id;
	private String name;
	private String alias;
	private String code;
	private String description;
	private List<Task> tasks;
	
	public Subprocedure() {
		tasks = new ArrayList<>();
	}
	
	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public String getDisplayName() {
		return getName();
	}
	
	public String getDescription() {
		return description;
	}
	
	public Procedure getProcedure() {
		return procedure;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public Integer getVersion() {
		return getProcedure().getVersion();
	}
	
	public List<RoleInProcess> getRoles() {
		return getProcedure().getRoles();
	}
	
	public Process start(WorkflowContext context) {
		return null;
	}
	
	public Procedure clone() {
		return null;
	}

	public Task getTask(String name) {
		for (Task task : getTasks()) {
			if (task.getId()!=null && task.getId().equals(name)) {
				return task;
			}
		}
		for (Task task : tasks) {
			if (task.getName().equals(name) || (((KbeeTask)task).getAlias()!=null && ((KbeeTask)task).getAlias().equals(name))) {
				return task;
			}
		}
		return null;
	}
	
	public Task getInitial() {
		for (Task task : getTasks()) {
			if (task.isInitial()) {
				return task;
			}
		}
		return null;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Activity initiate(WorkflowContext context) {
		return this.handle(StatesMachine.Initialization_Event, context);
	}
	
	public Activity handle(String event, WorkflowContext context) {
		return ((KbeeProcedure)getProcedure()).getStates().handle(event, context);
	}
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		return getProcedure().getRoles(context);
	}
	
	public Locale getLocale() {
		return getProcedure().getLocale();
	}
	
	public List<ProcedurePhase> getPhases() {
		return getProcedure().getPhases();
	}
	
	public ContentTemplate getContentTemplate() {
		return ((ContentProcedure)getProcedure()).getContentTemplate();
	}
	
	public List<ProcessLauncher> getProcessLaunchers() {
		return ((ContentProcedure)getProcedure()).getProcessLaunchers();
	}
	
	public Domain getDomain() {
		return ((DomainObject)getProcedure()).getDomain();
	}
	
	public void setDomain(Domain domain) {
		((DomainObject)getProcedure()).setDomain(domain);
	}
	
	public List<Procedure> getSubprocedures() {
		return null;
	}
	
	public void setSubprocedures(List<Procedure> procedures) {
	}
	
	public Procedure getMaster() {
		return getProcedure();
	}
	
	public void update() {
		setTasks(getTasks());
		getMaster().setSubprocedures(getMaster().getSubprocedures());
	}
}