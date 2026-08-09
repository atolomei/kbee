package com.novamens.wicket.markup.html.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class ErrorDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	
	private IModel<String> messagemodel;
	private IModel<String> titlemodel;
	
	

	public ErrorDialog(String id) {
		super(id, "title", "message", Dialog.OkError);
	}
													
	public void open(AjaxRequestTarget target, IModel<String> titlemodel,  IModel<String> messagemodel) {
		
		this.titlemodel = titlemodel;
		replace((new Label("title", getTitle())).setEscapeModelStrings(false));
			
		this.messagemodel = messagemodel;
		replace((new Label("message", getMessage())).setEscapeModelStrings(false));
		
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	
	
	public void open(AjaxRequestTarget target, IModel<String> messagemodel) {
		this.messagemodel = messagemodel;
		replace((new Label("message", getMessage())).setEscapeModelStrings(false));
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	@Override
	public IModel<String> getMessage() {
		return new Model<String>() {
			public String getObject() {
				return messagemodel!=null ? ErrorDialog.this.messagemodel.getObject() : "";
			}
		};
	}
	
	public void setTitleModel(IModel<String> title) {
		this.titlemodel=title;
	}
	
	public void setMessageModel(IModel<String> ms) {
		this.messagemodel=ms;
	}

	
	@Override
	public IModel<String> getTitle() {
		return new Model<String>() {
			public String getObject() {
				return titlemodel!=null ? ErrorDialog.this.titlemodel.getObject() : "";
			}
		};
	}
	
}