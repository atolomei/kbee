package kbee.web.content.workflow;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.workflow.WorkflowContext;

@SuppressWarnings("serial")
public class TaskTitlePanel<T extends Content> extends ModelPanel<WorkflowContext>  {
	private static final long serialVersionUID = 1L;
	
	public TaskTitlePanel(IModel<WorkflowContext> workflowmodel) {
		this("task-info", workflowmodel);
	}
	
	public TaskTitlePanel(String id, IModel<WorkflowContext> workflowmodel) {
		super(id, workflowmodel);
	}
	
	public void onInitialize() {
		super.onInitialize();
		add(new Label("title", new Model<String> () {
			public String getObject() {
				return getModelObject().getTask().getDisplayName();
			}
		}));
	}
}
