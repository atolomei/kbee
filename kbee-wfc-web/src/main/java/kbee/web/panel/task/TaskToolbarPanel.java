package kbee.web.panel.task;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.workflow.WorkflowContext;

public class TaskToolbarPanel<T extends Content> extends KBPanel {

	private static final long serialVersionUID = 1L;

	IModel<WorkflowContext> workflowmodel;
	
	public TaskToolbarPanel(String id, IModel<T> model, IModel<WorkflowContext> workflowmodel) {
		super(id, model);
		setOutputMarkupId(true);
		this.workflowmodel=workflowmodel;
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
	}
	
	
	public void onDetach() {
		super.onDetach();
		if (getWorkflowModel()!=null)
			getWorkflowModel().detach();
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return workflowmodel;
	}
		
}
