package com.novamens.wicket.markup.html.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;



@SuppressWarnings("serial")
public class ConfirmationDialog extends Dialog {
	private static final long serialVersionUID = 1L;
	
	private IModel<String> messagemodel;
	private IModel<String> textmodel;
	private IModel<String> titlemodel;

	public ConfirmationDialog(String id) {
		super(id, "confirmation.title", "confirmation.message", Dialog.Cancel, Dialog.Delete);
	}
	
	public void open(AjaxRequestTarget target, IModel<String> messagemodel, Button button, Handler handler) {
		open(target, null, messagemodel, null, button, handler);
	}
	
	public void open(AjaxRequestTarget target, IModel<String> titlemodel, IModel<String> messagemodel,  Button button, Handler handler) {
		open(target, titlemodel, messagemodel, null, button, handler);
	}
	
	public void open(AjaxRequestTarget target, IModel<String> titlemodel, IModel<String> messagemodel, IModel<String> textmodel,  Button button, Handler handler) {
		
		if (titlemodel!=null)
			this.titlemodel = titlemodel;
		
		this.messagemodel = messagemodel;
		this.textmodel = textmodel;
		
		replace(new Label("title", getTitle()));
		replace((new Label("message", getMessage())).setEscapeModelStrings(false));
		
		if (textmodel!=null)
			replace((new Label("text", getText())).setEscapeModelStrings(false));
		
		addButtons(Dialog.Cancel, button);
		
		setHandler(handler);
		
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	@Override
	public IModel<String> getTitle() {
		if (titlemodel!=null)
			return new Model<String>() {
				public String getObject() {
					return titlemodel!=null ? ConfirmationDialog.this.titlemodel.getObject() : "";
				}
			};
		else
			return super.getTitle();
	}
	
	@Override
	public IModel<String> getMessage() {
		return new Model<String>() {
			public String getObject() {
				return messagemodel!=null ? ConfirmationDialog.this.messagemodel.getObject() : "";
			}
		};
	}
	
	public IModel<String> getText() {
		return textmodel;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (titlemodel!=null)
			titlemodel.detach();
		if (messagemodel!=null)
			messagemodel.detach();
		if (textmodel!=null)
			textmodel.detach();
	}

	protected void onConfirm(AjaxRequestTarget target) {
		
	}
}