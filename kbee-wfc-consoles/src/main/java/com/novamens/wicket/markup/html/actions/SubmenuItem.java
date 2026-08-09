package com.novamens.wicket.markup.html.actions;

import java.util.List;


public interface SubmenuItem extends MenuItem {
	public List<MenuItem> getItems();
	public int getTop();
	public int getLeft();
}
