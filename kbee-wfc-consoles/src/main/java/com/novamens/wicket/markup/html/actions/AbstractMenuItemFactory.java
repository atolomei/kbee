package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.model.IModel;

public class AbstractMenuItemFactory<T> implements MenuItemFactory<T> {
	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	
	public AbstractMenuItemFactory(IModel<T> model) {
		this.model = model;
	}
	
	public AbstractMenuItemPanelV5<T> getItem(String id) {
		return null;
	};

	public IModel<T> getFactoryModel() {
		return model;
	};
	
	public IModel<T> getModel() {
		return model;
	};
}
