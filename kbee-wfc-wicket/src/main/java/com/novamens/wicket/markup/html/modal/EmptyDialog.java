package com.novamens.wicket.markup.html.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;



@SuppressWarnings("serial")
public class EmptyDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	
	private IModel<String> messagemodel;


	public EmptyDialog(String id) {
		super(id, "title", "message", Dialog.Ok);
	}
	
	public void open(AjaxRequestTarget target, IModel<String> messagemodel) {
		this.messagemodel = messagemodel;
		replace(new Label("message", getMessage()));
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	@Override
	public IModel<String> getMessage() {
		return new Model<String>() {
			public String getObject() {
				return messagemodel!=null ? EmptyDialog.this.messagemodel.getObject() : "";
			}
		};
	}
}