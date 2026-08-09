package com.novamens.wicket.markup.html.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class InfoDialog extends Dialog {
	private static final long serialVersionUID = 1L;

	private IModel<String> messagemodel;
	private IModel<String> titlemodel;
	
	public InfoDialog(String id) {
		super(id, "title", "message", Dialog.Ok_Default);
		super.setOutputMarkupId(true);
	}
	
	public void open(AjaxRequestTarget target,  IModel<String> messagemodel) {
		open(target, new Model<String>(""), messagemodel);
	}
	
	public void open(AjaxRequestTarget target, IModel<String> titlemodel,  IModel<String> messagemodel) {
		setTitleModel(titlemodel);
		replace((new Label("title", getTitle())).setEscapeModelStrings(false));
		setMessageModel(messagemodel);
		replace((new Label("message", getMessage())).setEscapeModelStrings(false));
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	public void setTitleModel(IModel<String> title) {
		this.titlemodel=title;
	}
	
	public void setMessageModel(IModel<String> ms) {
		this.messagemodel=ms;
	}
	
	@Override
	public IModel<String> getMessage() {
		return new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return messagemodel!=null ? InfoDialog.this.messagemodel.getObject() : "";
			}
		};
	}
	
	@Override
	public IModel<String> getTitle() {
		return new Model<String>() {
			public String getObject() {
				return titlemodel!=null ? InfoDialog.this.titlemodel.getObject() : "";
			}
		};
	}
}