package com.novamens.content.web.base.page.component;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.ResourceContainer;

import kbee.util.PropertiesFactory;
			
public class KbeeResourceContainerWebPage extends com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage {
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

 	private static final long serialVersionUID = -3253180023604022129L;

	private IModel<ResourceContainer> model;
	
	public KbeeResourceContainerWebPage() {
		this(null);
	}
	
	public KbeeResourceContainerWebPage(IModel<ResourceContainer> model) {
		this(model, null, null);
	}
	
	
	public KbeeResourceContainerWebPage(IModel<ResourceContainer> model, IModel<String> keywords, IModel<String> description) {
		
		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);
		if (keywords!=null) 
			setPageKeywords(keywords.getObject());
		setPageDescription(description);
		setModel(model);
	}
	
	
	public void onDetach() {		
		if (model!=null)
			model.detach();
		super.onDetach();
	}

	public IModel<ResourceContainer> getModel() {
		return model;
	}
	
	public ResourceContainer getModelObject() {
		return getModel().getObject();
	}
	
	public void setModel(IModel<ResourceContainer> model) {
		this.model = model;
	}
	
}
