package com.novamens.kbee.wicket.markup.html.console.browser;


import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.console.BaseBrowser;

public class GridMenu extends ToolbarItem {

	private static final long serialVersionUID = 1L;
	
	ContextMenuPanel<Void> menu;

	public GridMenu(BaseBrowser<?> browser ) {
		super(browser, Align.TOP_RIGHT);
		setOutputMarkupId(true);
		menu = new ContextMenuPanel<>( null);
		add(menu);
	}


	public void addItem(MenuItemFactory itemFactory){
		menu.addItem(itemFactory);
	}

}
