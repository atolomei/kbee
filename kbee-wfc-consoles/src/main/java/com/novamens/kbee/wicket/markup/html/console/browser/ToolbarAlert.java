package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import kbee.web.console.BaseBrowser;

public class ToolbarAlert extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	public ToolbarAlert(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add((new Label("label", getLabel())).setEscapeModelStrings(false));
	}

	
	protected IModel<String> getLabel() {
		return null;
	}
	
}
