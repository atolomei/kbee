package com.novamens.content.web.security.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
//import com.novamens.security.acl.Acl;
//import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.modal.Modal;

import kbee.web.security.AclPanel;

@SuppressWarnings("serial")
public class AclModal<T extends Content> extends Modal {
	private static final long serialVersionUID = 1L;
	
	public AclModal(String id) {
		super(id);

		setTitle("modal.acl.title");

		setModalType(Modal.MODAL_FULL_SCREEN); 
		
		setBody(new Panel("body") {
			
		});

		setButtons(Modal.Close);
	}
	
	public void open(AjaxRequestTarget target, IModel<T> model) {

		setParameters(model.getObject().getTitle());
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		
		title.setEscapeModelStrings(false);		
		
		modal_dialog.addOrReplace(title);
		
		setBody(new AclPanel<T>("body", model));
		
		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});	
	}
}
