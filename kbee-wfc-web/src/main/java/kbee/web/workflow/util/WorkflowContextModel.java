package kbee.web.workflow.util;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

public class WorkflowContextModel<T extends Content> implements IModel<WorkflowContext> {
	private static final long serialVersionUID = 1L;
	private WorkflowContext context;
	private IModel<T> model;
	
	@SuppressWarnings("unchecked")
	public WorkflowContextModel(WorkflowContext context) {
		this.context = context;
		setModel(new ObjectModel<T>((T)((KbeeContext)context).getContent()));
	}
	
	public WorkflowContextModel(WorkflowContext context, IModel<T> model) {
		this.context = context;
		this.model = model;
	}
	
	public WorkflowContext getObject() {
		if (context!=null) return context;
		WorkflowService workflowService = getModel().getObject().getService(WorkflowService.class);
		context = workflowService.getContext();
		return context;
	}
	
	public void setObject(WorkflowContext context) {
		this.context = context;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public void detach() {
		context = null;
		model.detach();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		context = null;
		oos.defaultWriteObject();
	}
}