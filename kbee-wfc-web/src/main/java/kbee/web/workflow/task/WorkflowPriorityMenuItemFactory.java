package kbee.web.workflow.task;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.workflow.Priority;
import com.novamens.workflow.WorkflowContext;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.workflow.util.WorkflowContextModel;

public class WorkflowPriorityMenuItemFactory<T extends Content> implements MenuItemFactory<T> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkflowPriorityMenuItemFactory.class.getName());
	
	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	private IModel<Priority> priority_model;
	
	public WorkflowPriorityMenuItemFactory(IModel<T> model,  IModel<Priority> mpriority) {
		this.model = model;
		this.priority_model=mpriority;
		model.detach();
	}
	
	public AbstractMenuItemPanelV5<T> getItem(String id) {

		
		return new WorkflowPriorityMenuItem<T>(id, model, priority_model,getWorkflowModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				try {
					WorkflowPriorityMenuItemFactory.this.onUpdate(target);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<>(target, e));
				} 
			}
		};
	}

	
	public void detach() {
		this.model.detach();
		this.priority_model.detach();
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	
	protected IModel<WorkflowContext> getWorkflowModel() {
		WorkflowService workflowService = this.model.getObject().getService(WorkflowService.class);
		if (workflowService!=null) {
			WorkflowContext workflowcontext = workflowService.getContext();
			IModel<WorkflowContext> workflowmodel  =  new WorkflowContextModel<T>(workflowcontext);
			return workflowmodel;
		}
		else
			return null;
	}
	

	
}
