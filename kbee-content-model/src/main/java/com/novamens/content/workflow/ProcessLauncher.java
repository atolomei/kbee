package com.novamens.content.workflow;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LauncherGroup;
import com.novamens.dom.DomainObject;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.Acl;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.WorkflowContext;

public interface ProcessLauncher extends DomainObject, Identifiable {

	public String getLabel();
	public String getDescription();
	
	public LauncherGroup getLauncherGroup();
	public ContentTemplate getContentTemplate();
	public Procedure getProcedure();
	
	public Process startProcess() throws ContentCreationException, ContentMgmtException;
	public Process startProcess(Content template) throws ContentCreationException, ContentMgmtException;
	public Process startProcess(WorkflowContext context);
	public Process startProcess(WorkflowContext context, Object initialData);
	
	public boolean executeable();
	
	public boolean isLibrary();
	public boolean isEnabled();
	public boolean isApiEnabled();
	public boolean isMobile();
	
	public boolean isEnabled(Content content);
	public boolean useTemplate();

	public Acl getAcl();
}