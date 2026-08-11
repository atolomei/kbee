package com.novamens.content.workflow;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LauncherGroup;
import com.novamens.dom.ObjectState;
import com.novamens.service.ObjectService;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;

public interface WorkflowDomainService extends ObjectService {
	
	public Process startProcess(ProcessLauncher launcher);
	public Process startProcess(ProcessLauncher launcher, Content template);
	public Process startProcess(String launcherlabel);
	
	public void update(Procedure procedure, List<String> parts);
	public void update(Procedure procedure, String description);
	
	public List<Procedure> getProcedures();
	public List<Procedure> getProceduresLibrary();
	
	public List<ProcessLauncher> getLaunchers();
	public List<ProcessLauncher> getContextLaunchers(Content content);
	public List<ProcessLauncher> getContextLaunchers(ContentTemplate template);
	
	public List<ProcessLauncher> getLaunchers(ObjectState enabled);
	public List<LauncherGroup> getLauncherGroups();
	
	public Procedure getProcedureBean(String key);
	
	public Procedure createProcedure(ContentTemplate template, Procedure protoype);
	
	public ProcessLauncher createLauncher(Procedure procedure);
	public void deleteLauncher(ProcessLauncher launcher);
}