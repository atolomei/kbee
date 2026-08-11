package com.novamens.content.workflow;

import java.util.List;

import com.novamens.content.model.ContentTemplate;
import com.novamens.workflow.Procedure;

public interface ContentProcedure extends Procedure {
	public ContentTemplate getContentTemplate();
	public List<ProcessLauncher> getProcessLaunchers();
}