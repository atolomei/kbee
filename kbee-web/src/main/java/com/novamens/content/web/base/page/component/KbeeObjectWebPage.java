package com.novamens.content.web.base.page.component;

import org.apache.wicket.model.IModel;

import kbee.util.PropertiesFactory;

@SuppressWarnings("serial")
public class KbeeObjectWebPage<T> extends com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage {
	
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	 
	private IModel<String> pageTitle;
	
	public KbeeObjectWebPage() {
		this(null);
	}
	
	public KbeeObjectWebPage(IModel<T> model) {
		this(null, model, "", "");
	}
	
	public KbeeObjectWebPage ( IModel<String> title, IModel<T> model, String keywords, String description) {
		
		setPageFonts(getFonts());
		
		setPageXUACompatible(XUA_Compatible);
		setModel(model);
		add(new KbeeObjectHeader<T>("header") {
			public IModel<T> getModel() {
				return KbeeObjectWebPage.this.getModel();
			}
		});
		add((new KbeeContentFooter("footer")).setVisible(false));
	}
	
	public void setPageTitle( IModel<String> title) {
		pageTitle = title;
	}
	
	public IModel<String> getPageTitle() {
		return pageTitle;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
}