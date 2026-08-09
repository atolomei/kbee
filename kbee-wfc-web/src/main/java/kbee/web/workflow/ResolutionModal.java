package kbee.web.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.workflow.WorkflowContext;

public class ResolutionModal extends Modal {
	private static final long serialVersionUID = 1L;

	public ResolutionModal() {
		super("resolution-modal");
		
		setTitle("modal.resolution.title");
		setBody(new ResolutionPreviewPanel("body"));
		setButtons(new Button("button.ok", "btn btn-sm btn-default"));
		setModalType(Modal.MODAL_CENTER);
	}

	public void open(AjaxRequestTarget target, IModel<WorkflowContext> workflowmodel, Handler handler, String... parameter) {
		
		((ResolutionPreviewPanel)getBody()).setModel(new Model<String>(workflowmodel.getObject().getResolution()));
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		String task = workflowmodel.getObject().getTask().getName();
		Label title = new Label("title", task + ". Task Resolution ");
		title.setEscapeModelStrings(false);		
		modal_dialog.addOrReplace(title);
		
		super.open(target, handler, parameter);
	}
	
	public void open(AjaxRequestTarget target, String resolution, Handler handler, String... parameter) {
		
		((ResolutionPreviewPanel)getBody()).setModel(new Model<String>(resolution));

		if (parameter.length>0) {
			WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
			Label title = new Label("title", parameter[0]);
			title.setEscapeModelStrings(false);		
			modal_dialog.addOrReplace(title);
		}
		
		super.open(target, handler, parameter);
	}
}