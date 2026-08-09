package com.novamens.wicket.markup.html.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;


import com.novamens.kbee.wicket.util.InvisiblePanel;

public class SeparatorTabKB extends AbstractTabKB {

	private static final long serialVersionUID = 1L;

	public SeparatorTabKB(String key) {
			super(null, key);
			setCss("separator");
	}
	
	@Override
	public String getKey() {
		return "separator";
	}
	
	@Override
	public boolean isLink() {
		return false;
	}
	
	@Override
	public boolean isVisible()
	{
		return true;
	}
	
	
	@Override
	public WebMarkupContainer getPanel(String panelId) {
		return new InvisiblePanel(panelId);
	}

	@Override
	public String getStyle() {
		return "margin-top:10px; width:100%; float:left; border-top:1px solid #ededef; margin-bottom:10px;";
	}
}
