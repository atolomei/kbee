package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class IconButton<T> extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	public IconButton(BaseBrowser<T> browser, Align align) {
		super(browser, align);
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addLink();
	}
	
	protected void onClick() {
	}
	
	protected String getIcon() {
		return "fal fa-tasks";
	}
	
	protected void addLink() {
		
		Link<Void> link = new Link<Void>("link") {
			@Override
			public void onClick() {
				IconButton.this.onClick();
			}
			@Override
			public boolean isEnabled() {
				return IconButton.this.isEnabled();
			}
		};
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		
		icon.add(new AttributeModifier("class", getIcon())); 
		
		link.add(icon);
		
		link.add(new AttributeModifier("target", "_blank"));

		add(link);
	}
}
