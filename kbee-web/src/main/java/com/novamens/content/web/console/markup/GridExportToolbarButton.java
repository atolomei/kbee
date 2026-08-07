package com.novamens.content.web.console.markup;



import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;

import kbee.web.console.BaseBrowser;

public abstract class GridExportToolbarButton extends AjaxToolbarButton {

	private static final long serialVersionUID = 1L;
	
	public GridExportToolbarButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}



}
