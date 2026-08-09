package com.novamens.workflow;

import java.time.OffsetDateTime;
import java.util.Map;

import com.novamens.security.User;

public interface WorkflowContext {

	public Factory getFactory();
	
	public Process getProcess();
	public Procedure getProcedure();
	public Task getTask();
	public Activity getCurrentActivity();
	public ProcedurePhase getCurrentPhase();
	public Priority getPriority();
	public Reason getReason();
	public OffsetDateTime getTime();
	public String getNote();
	public String getState();
	public boolean isPending();
	public boolean isApi();
	
	public Map<RoleInProcess, User> getRoles();
	
	public String getResolution(); // Letter 
	public String getResolutionTitle(); //  
	
	public OffsetDateTime getDueDate();
	
	public String getParameter(String name);
	public void setParameter(String name, String value);
	
	public Object getInitialData();
}