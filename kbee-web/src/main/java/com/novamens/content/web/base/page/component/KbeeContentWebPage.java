package com.novamens.content.web.base.page.component;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;

import kbee.util.PropertiesFactory;


public class KbeeContentWebPage<T extends Content> extends AbstractKbeeWebPage {
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
	
	public KbeeContentWebPage() {
		this(null);
	}
	
	public KbeeContentWebPage (IModel<T> model) {
		
		setPageFonts(getFonts());
		
		setModel(model);
		
		setPageXUACompatible(XUA_Compatible);
		
		if (model!=null)
			setPageTitle(new Model<String>(getModel().getObject().getTitle()));
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public T getModelObject() {
		return model.getObject();
	}
	
	public void onDetach() {
		
		if (model!=null)
			model.detach();
		
		super.onDetach();
	}
}