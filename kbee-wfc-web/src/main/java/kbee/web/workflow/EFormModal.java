package kbee.web.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormData;
import com.novamens.wicket.markup.html.modal.Modal;

public class EFormModal extends Modal {
	private static final long serialVersionUID = 1L;

	public EFormModal() {
		super("eform-modal");
		setTitle("modal.eform.title");
		setBody(new EFormViewerPanel("body"));
		setButtons(new Button("button.ok", "btn btn-sm btn-default"));
		setModalType(Modal.MODAL_CENTER);
	}

	public void open(AjaxRequestTarget target, IModel<EFormData> model, Handler handler, String... parameter) {
		
		((EFormViewerPanel)getBody()).setModel(model);
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		Label title = new Label("title","EForm");
		title.setEscapeModelStrings(false);		
		modal_dialog.addOrReplace(title);
		
		super.open(target, handler, parameter);
	}
	
//	public void open(AjaxRequestTarget target, String resolution, Handler handler, String... parameter) {
//		
//		((ResolutionPreviewPanel)getBody()).setModel(new Model<String>(resolution));
//
//		if (parameter.length>0) {
//			WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
//			Label title = new Label("title", parameter[0]);
//			title.setEscapeModelStrings(false);		
//			modal_dialog.addOrReplace(title);
//		}
//		
//		super.open(target, handler, parameter);
//	}
}