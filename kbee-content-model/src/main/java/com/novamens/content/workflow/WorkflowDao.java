package com.novamens.content.workflow;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.LauncherGroup;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

public interface WorkflowDao  extends Dao {
	
	public Process findProcessById(Long id);
	public Activity findActivityById(Long id);
	
	public boolean hasProcesses(Procedure procedure);
	public boolean hasActivities(Task task);
	public List<Procedure> getProcedures(Domain domain);
	
	public Process getActiveProcess(Content content);
	public Procedure getProcedure(Serializable id);
	public Procedure findProcedureById(Serializable id);
	public ProcessLauncher getProcessLauncher(Serializable id);
	
	public List<ProcessLauncher> getLaunchers(Domain domain);
	public List<ProcessLauncher> getLaunchers(Domain domain, ObjectState state);
	public List<ProcessLauncher> getLaunchers(LauncherGroup group);
	public List<ProcessLauncher> getLaunchers(Procedure procedure);
	
	public List<LauncherGroup> getLauncherGroups(Domain domain, ObjectState state);
	public List<ProcessLauncher> getLaunchers(LauncherGroup group, ObjectState state);
	
	public void update(Process process);
	public void update(WorkflowContext process);
	public void update(Procedure procedure);
	public void update(Activity activity);
	
	public void delete(ProcessLauncher launcher);
	
	public void refresh(Long contentId);
	public WorkflowContext reload(WorkflowContext context);
	public Object reload(Object object);
	
	public void evict();
}