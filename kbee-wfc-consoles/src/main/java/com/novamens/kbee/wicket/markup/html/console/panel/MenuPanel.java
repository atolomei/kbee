package com.novamens.kbee.wicket.markup.html.console.panel;

import java.util.List;

import com.novamens.wicket.markup.html.actions.MenuItemFactory;

public interface MenuPanel<T> {
	public List<MenuItemFactory<T>> getItems();
	public boolean isEmpty();
}