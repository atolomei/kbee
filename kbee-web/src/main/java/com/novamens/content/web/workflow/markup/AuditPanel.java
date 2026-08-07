package com.novamens.content.web.workflow.markup;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.WorkflowContext;

/**
 * 
 * This is for Content as part of a Workflow: 
 * Process History
 *
 * @param <T>
 */
public class AuditPanel<T extends Content> extends ModelPanel<WorkflowContext> {
				
	private static final long serialVersionUID = 1L;

	public AuditPanel(String id, IModel<WorkflowContext> workflowmodel) {
		super(id, workflowmodel);
		setOutputMarkupId(true);
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		// add(new ProcessHistoryPanel<T>(super.getModel()));
		throw new KbeeRuntimeException("not implemented !!!!!");
	}
}
