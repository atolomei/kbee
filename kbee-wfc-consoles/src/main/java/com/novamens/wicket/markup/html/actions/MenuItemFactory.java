package com.novamens.wicket.markup.html.actions;

import java.io.Serializable;

 
public interface MenuItemFactory<T> extends Serializable {
	
	public abstract AbstractMenuItemPanelV5<T> getItem(String id);
	public default void detach() {};
	public default int getOrder() {return 0;}
}
