  package com.novamens.workflow;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.novamens.security.Identifiable;
import com.novamens.security.Principal;

public interface Procedure extends Identifiable {
	
	public String getName();
	
	public String getCode();
	
	public String getAlias();
	
	public String getDisplayName();
	
	public String getDescription();
	
	public Integer getVersion();
	
	public List<RoleInProcess> getRoles();
	
	public Process start(WorkflowContext context);
	
	public Task getTask(String name);
	public List<Task> getTasks();
	public void setTasks(List<Task> tasks);
	public Task getInitial();
	
	public Activity initiate(WorkflowContext context);
	public Activity handle(String event, WorkflowContext context);
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context);
	
	public List<ProcedurePhase> getPhases();
	
	public List<Procedure> getSubprocedures();
	public void setSubprocedures(List<Procedure> procedures);
	
	public Procedure getMaster();
	
	public Locale getLocale();
	
	public Procedure clone();
}