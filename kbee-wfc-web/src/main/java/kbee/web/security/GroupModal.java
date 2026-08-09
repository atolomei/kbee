package kbee.web.security;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.security.acl.Group;
import com.novamens.wicket.markup.html.modal.Modal;

@SuppressWarnings("serial")
public class GroupModal extends Modal {
	private static final long serialVersionUID = 1L;
	
	public GroupModal(String id) {
		super(id);
		setTitle("modal.group.title");
		setBody(new GroupPanel("body"));
		setButtons(Modal.Close);
	}
	
	public void open(AjaxRequestTarget target, IModel<Group> model) {

		setParameters(model.getObject().getName());
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		Label title = new Label("title", getTitle());
		title.setEscapeModelStrings(false);		
		modal_dialog.addOrReplace(title);
		((GroupPanel)getBody()).setModel(model);
		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});	
	}
}
