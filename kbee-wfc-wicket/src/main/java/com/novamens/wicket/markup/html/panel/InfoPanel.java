package com.novamens.wicket.markup.html.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class InfoPanel<T> extends Panel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<T> model;

	public InfoPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	
	public InfoPanel(String id, IModel<String> title, IModel<String> text) {
		super(id);
		setTitle(title);
		setText(text);
		setOutputMarkupId(true);
	}
	
	
	
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
	
	
	public IModel<T> getModel() {
		return model;
	}


	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new Label("title", getTitle()));
		add(new Label("text", getText()));
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}


	
	
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	
	
}
