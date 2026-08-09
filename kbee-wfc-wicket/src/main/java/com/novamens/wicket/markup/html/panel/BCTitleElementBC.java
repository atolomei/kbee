package com.novamens.wicket.markup.html.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.util.IBCElement;

public class BCTitleElementBC extends WebMarkupContainer implements IBCElement {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IModel<String> html_title;
	private IModel<String> model;
	
	public BCTitleElementBC(IModel<String> model) {
		super("link", model);
		this.model=model;
	}

	
	public BCTitleElementBC(String id, IModel<String> model) {
		super(id, model);
		this.model=model;
	}

	
	public void onInitialize() {
		super.onInitialize();
		add((new Label("label", getLabel())).setEscapeModelStrings(false));
		if (this.getHTMLTitleAttribute()!=null)
			this.add( new AttributeModifier("title", getHTMLTitleAttribute()));

		
	}
	
	
	@Override
	public IModel<String> getLabel() {
		return model;
	}


	@Override
	public void onClick() {
	}
	
	
	

	public void setHTMLTitleAttribute(IModel<String> ht) {
		html_title=ht;
	}
	
	@Override
	public IModel<String> getHTMLTitleAttribute() {
		return this.html_title;
	}


	@Override
	public boolean isNewTab() {
		return false;
	}



}
