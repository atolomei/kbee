package com.novamens.wicket.markup.html.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

public class ContextTwoLevesMenuPanel<T> extends Panel {
									
	
	private static final long serialVersionUID = 1L;
	private IModel<T> model;
	private int index;
	private List<MenuItemFactory<T>> items = new ArrayList<MenuItemFactory<T>>();
	
	public ContextTwoLevesMenuPanel(IModel<T> model) {
			this("menu", model);
	}
	
	public ContextTwoLevesMenuPanel(String id, IModel<T> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
		add( new InvisiblePanel("submenu"));
	}
	
	
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("item")==null) {
			ListView<MenuItemFactory<T>> itemsview = new ListView<MenuItemFactory<T>>("item", this.items) {
				private static final long serialVersionUID = 1L;
				public void populateItem(ListItem<MenuItemFactory<T>> factoryitem) {
					AbstractMenuItemPanelV5<T> item = factoryitem.getModelObject().getItem("panel");
					item.setMarkupId("panel"+factoryitem.getIndex());
					item.setVisible(factoryitem.isVisible());
					item.setIndex(getIndex());
					item.setModel(new IModel<T>() {
						private static final long serialVersionUID = 1L;
						public void setObject(T object) {
							ContextTwoLevesMenuPanel.this.getModel().setObject(object);
						}
						public T getObject() {
							return ContextTwoLevesMenuPanel.this.getModel().getObject();
						}
						public void detach() {
							if (ContextTwoLevesMenuPanel.this.getModel()!=null)
								ContextTwoLevesMenuPanel.this.getModel().detach();
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
	
	public void addItem(MenuItemFactory<T> item) {
		this.items.add(item);
	}	 
	
	public void onDetach() {
		super.onDetach();
		if (this.model!=null) 
			this.model.detach();
	}
}
