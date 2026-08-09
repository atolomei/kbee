package com.novamens.wicket.markup.html.tabs;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

public class AbstractModelTab<T> extends AbstractTab implements IDetachable {

	
	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
	
	public AbstractModelTab(IModel<T> model, IModel<String> title) {
		super(title);
		this.model=model;
	}
	
	
	public void detach() {
		model.detach();
	}
	
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	@Override
	public WebMarkupContainer getPanel(String panelId) {
		return null;
	}

}
