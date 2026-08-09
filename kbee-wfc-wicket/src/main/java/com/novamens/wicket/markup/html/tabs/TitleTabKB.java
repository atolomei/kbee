package com.novamens.wicket.markup.html.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

public class TitleTabKB extends AbstractTabKB {

	private static final long serialVersionUID = 1L;

	public TitleTabKB(String key, IModel<String> title, String css) {
			super(title, key, css);
	}
	
	@Override
	public boolean isLink() {
		return false;
	}
	
	@Override
	public WebMarkupContainer getPanel(String panelId) {
		return new InvisiblePanel(panelId);
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}
	
	@Override
	public String getStyle() {
		return null ; //"margin-top:10px; width:100%; float:left; border-top:1px solid #ededef; margin-bottom:10px;";
	}
	
}
