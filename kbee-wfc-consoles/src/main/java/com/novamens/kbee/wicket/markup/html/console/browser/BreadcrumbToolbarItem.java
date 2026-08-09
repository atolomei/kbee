package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.console.BaseBrowser;

public class BreadcrumbToolbarItem extends ToolbarItem {

	private static final long serialVersionUID = 1L;
	
	public BreadcrumbToolbarItem(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	public BreadcrumbToolbarItem(BaseBrowser<?> browser, Align align, Panel bcrumb) {
		super(browser, align);
		setPanel(bcrumb);
	}
	
	public void setPanel(Panel breadcrumb) {
		addOrReplace(breadcrumb);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("breadcrumb")== null)
			add( new InvisiblePanel("breadcrumb"));
	}
	
}
