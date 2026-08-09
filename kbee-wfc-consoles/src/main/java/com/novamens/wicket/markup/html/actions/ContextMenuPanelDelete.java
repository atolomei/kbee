package com.novamens.wicket.markup.html.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.actions.ContextMenuPanelDelete;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

@SuppressWarnings("serial")
public class ContextMenuPanelDelete<T> extends Panel  {
	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	private int height;
	private int delta;
	private int index;
	private List<MenuItemFactory<T>> items = new ArrayList<MenuItemFactory<T>>();

	@Deprecated
	public ContextMenuPanelDelete(IModel<T> model, int height) {
		this(model, height, -30);
	}
	
	@Deprecated
	public ContextMenuPanelDelete(IModel<T> model, int height, int delta) {
		this(model);
	}
	
	public ContextMenuPanelDelete(IModel<T> model) {
		super("menu");
		
		setModel(model);

		setOutputMarkupId(true);
		
		ListView<MenuItemFactory<T>> itemsview = new ListView<MenuItemFactory<T>>("item", items) {
			public void populateItem(ListItem<MenuItemFactory<T>> factoryitem) {
				AbstractMenuItemPanelV5<T> item = factoryitem.getModelObject().getItem("panel");
				item.setMarkupId("panel"+factoryitem.getIndex());
				item.setVisible(factoryitem.isVisible());
				item.setIndex(getIndex());
				item.setModel(new IModel<T>() {
					public void detach() {
						if (ContextMenuPanelDelete.this.getModel()!=null)
						ContextMenuPanelDelete.this.getModel().detach();
					}
					public void setObject(T object) {
						ContextMenuPanelDelete.this.getModel().setObject(object);
					}
					public T getObject() {
						return ContextMenuPanelDelete.this.getModel().getObject();
					}
				});
				
				if (item.getCssClass()!=null)
					factoryitem.add(new AttributeModifier("class", item.getCssClass()));
				
				factoryitem.add(item);
				factoryitem.setVisible(item.isVisible());
			}
		};
		
		add(itemsview);
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	@Deprecated
	public int getHeight() {
		return this.height;
	}
	
	@Deprecated
	public int getDelta() {
		return this.delta;
	}
	
	public void addItem(MenuItemFactory<T> item) {
		items.add(item);
	}	 
	
	public void onDetach() {
		super.onDetach();
		if (model!=null) model.detach();
	}
}
