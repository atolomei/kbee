package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

public class InfoEvent extends AbstractWicketAjaxEvent {
	
	public IModel<String> getTitle() {
		return title;
	}


	public void setTitle(IModel<String> title) {
		this.title = title;
	}


	public IModel<String> getText() {
		return text;
	}


	public void setText(IModel<String> text) {
		this.text = text;
	}


	IModel<String> title;
	IModel<String> text;
	


	String css;
	
	public InfoEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
		css="info";
	}
	
	
	public InfoEvent(AjaxRequestTarget requestTarget, IModel<String> title, IModel<String> text) {
		super(requestTarget);
		this.title=title;
		this.text=text;
		css="info";
	}

	public InfoEvent(AjaxRequestTarget requestTarget, IModel<String> title, IModel<String> text, String css) {
		super(requestTarget);
		this.title=title;
		this.text=text;
		this.css=css;
	}
	

	public void setCss(String css) {
		this.css = css;
	}
	
	public String getCss() {
		return this.css;
	}
 

}
