package com.novamens.wicket.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class HREFBCElement extends Panel implements IBCElement {

	private static final long serialVersionUID = 1L;
	
	private String url;
	protected IModel<String> label;
	private IModel<String> html_title;
	

	public HREFBCElement(String url) {
		super("link");
		this.url=url;
	}
	
	public HREFBCElement(String url, IModel<String> label) {
		super("link");
		this.url=url;
		this.label=label;
	}
	
	
	public HREFBCElement(String id, String url, IModel<String> label) {
		super(id);
		this.url=url;
		this.label=label;
	}
	
	@Override
	public IModel<String> getLabel() {
	return label;
	}
	
	public String getUrl() {
		return this.url;
	}

	@Override
	public boolean isNewTab() {
		return false;
	}
	
	
	protected void setLabel(IModel<String> label) {
	this.label=label;
	}
	
	@Override
	public void onClick() {
		// not used;
	}
	
	public void onInitialize() {
		super.onInitialize();
		WebMarkupContainer ln=new WebMarkupContainer("link");
		ln.add(new AttributeModifier("href", this.url));
		add(ln);
		Label la=new Label("label", getLabel());
		la.setEscapeModelStrings(false);
		ln.add(la);
		
		if (this.getHTMLTitleAttribute()!=null)
			this.add( new AttributeModifier("title", getHTMLTitleAttribute()));

		
	}
	


	public void setHTMLTitleAttribute(IModel<String> ht) {
		html_title=ht;
	}
	
	@Override
	public IModel<String> getHTMLTitleAttribute() {
		return this.html_title;
	}

	
}
