package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

public class InfoDialogEvent extends AbstractWicketAjaxEvent {

	IModel<String> title;
	IModel<String> text;
	
	public InfoDialogEvent(AjaxRequestTarget requestTarget, IModel<String> title, IModel<String> text) {
		super(requestTarget);
		
		this.title=title;
		this.text=text;
	}
	
	public IModel<String> getTitle() {
		return title;
	}
	
	public IModel<String> getText() {
		return text;
	}
	

}
