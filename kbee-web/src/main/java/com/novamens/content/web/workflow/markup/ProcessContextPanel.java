package com.novamens.content.web.workflow.markup;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.workflow.WorkflowContext;

@Deprecated
public class ProcessContextPanel<T extends Content> extends ModelPanel<WorkflowContext>  {
	private static final long serialVersionUID = 1L;
	
	public ProcessContextPanel(String id, IModel<WorkflowContext> workflowmodel) {
		super(id, workflowmodel);
		
		add(new ProcessInfoPanel(workflowmodel));
		 
	}
}
