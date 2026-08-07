package com.novamens.content.web.workflow.markup;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.security.acl.Group;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

public class AssignationModal<T extends Content> extends Modal {
	private static final long serialVersionUID = 1L;

	
	public AssignationModal() {
		this("assignation-modal");
	}
		
	public AssignationModal(String id) {
		super(id);
		
		setTitle("modal.assignation.title");

		setBody(new AssignationPanel<T>("body"));
																	
		setButtons( new Button("modal.assignation.cancel", "btn btn-sm btn-default", ButtonType.CANCEL), new Button("modal.assignation.submit", "btn btn-sm btn-primary", ButtonType.SUBMIT));
		
		setModalType(Modal.MODAL_CENTER);
	}
	
	
	@SuppressWarnings("unchecked")
	public void open(AjaxRequestTarget target, IModel<WorkflowContext> workflowmodel, Handler handler, List<Group> groups, String... parameter) {
		
		IModel<T> model = new ObjectModel<T>((T)((KbeeContext)workflowmodel.getObject()).getContent());
		
		((AssignationPanel<T>)getBody()).setModel(model);
		((AssignationPanel<T>)getBody()).setEnabledGroups(groups);
		
		super.open(target, handler, parameter);
	}
}
