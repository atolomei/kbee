package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;

@SuppressWarnings("serial")
public abstract class ScriptMenuItemPanel<T> extends  MenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	public ScriptMenuItemPanel(String id) {
		super(id);
	}
	
	@Override
	public void onClick() {
	}
	
	protected abstract String onClickScript();
	
	@Override
	protected AbstractLink getNewLink(String id) {
		Link<?> link = new Link<Void>(id) {
			public void onClick() {
			}
		};
		link.add(new AttributeModifier("onclick", onClickScript()));
		link.setOutputMarkupId(true);
		return link;
	}
}
