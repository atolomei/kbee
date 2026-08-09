package com.novamens.wicket.markup.html.tabs;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

public class AbstractTabWithIcon extends AbstractTab {

	
	private static final long serialVersionUID = 1L;
	
	private String icon_class;
	private String icon_title;
	
	public AbstractTabWithIcon(IModel<String> title, String icon_class) {
		super(title);
		this.icon_class=icon_class;
	}

	
	public AbstractTabWithIcon(IModel<String> title, String icon_class, String icon_title) {
		super(title);
		this.icon_class=icon_class;
		this.icon_title=icon_title;
	}

	@Override
	public WebMarkupContainer getPanel(String panelId) {
		return null;
	}

	public String getIconTitle() {
		return icon_title;
	}
	
	public String getIconClass() {
		return icon_class;
	}
}
