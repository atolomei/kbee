package com.novamens.wicket.markup.html.actions;


import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;


public class MenuItemSelectionActionPanel<T> extends MenuItemPanelV5<T> {

	private static final long serialVersionUID = 1L;
	
	private BaseBrowser<T> browser;

	public MenuItemSelectionActionPanel(String id, BaseBrowser<T> browser) {
		super(id);
		this.browser=browser;
		setOutputMarkupId(true);
	}

	
	@Override
	public String getLabel() {
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(getBrowser().getPanel(GridPanel.class));
			}
		});
	}
	
	
	public BaseBrowser<T> getBrowser() {
		return this.browser;
	}
	
	@Override
	public boolean isEnabled() {
		return !getBrowser().getSelection().isEmpty();
	}
	
	

}
