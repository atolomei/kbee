package com.novamens.wicket.markup.html.tabs;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

public abstract class AbstractTabKB extends AbstractTab implements ITabKB {

	private static final long serialVersionUID = 1L;

	private String key;
	private String css;
	private String style;
	
	protected void setCss(String css) {
		this.css=css;
	}
	
	
	public AbstractTabKB(IModel<String> title) {
		super(title);
		this.key=null;
	}
	
	public AbstractTabKB(IModel<String> title, String key) {
		super(title);
		this.key=key;
	}

	public AbstractTabKB(IModel<String> title, String key, String css) {
		super(title);
		this.key=key;
		this.css=css;
	}
	
	@Override
	public WebMarkupContainer getPanel(String panelId) {
		return null;
	}
	
	@Override
	public String getKey() {
		return key==null?getTitle().getObject():key;
	}
	
	@Override
	public String getCss() {
		return css;
	}
	
	@Override	
	public String getStyle() {
		return style;
	}
	
	@Override
	public boolean isLink() {
		return true;
	}
}
