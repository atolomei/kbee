package com.novamens.wicket.markup.html.tabs;

import org.apache.wicket.extensions.markup.html.tabs.ITab;

public interface ITabKB extends ITab {

	public String getKey();
	
	public boolean isLink();
	
	public String getCss();
	public String getStyle();
	
	
}
