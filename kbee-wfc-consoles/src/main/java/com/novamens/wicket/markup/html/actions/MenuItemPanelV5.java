package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;

@SuppressWarnings("serial")
public abstract class MenuItemPanelV5<T> extends AbstractLinkMenuItemPanelV5<T> {
					
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MenuItemPanelV5.class.getName());

	public MenuItemPanelV5(String id) {
		super(id);
	}
	
	public String getTarget() {
		return null;
	}
	
	public boolean getOpener() {
		return false;
	}
	
	public String getCssClass() {
		return null;
	}
	
	public String getBeforeClick() {
		return null;
	}
	
	public void onClick() throws Exception {
	}
	
	public void onClick(AjaxRequestTarget target) throws Exception {
		
	}
	
	public PopupSettings getPopupSettings() {
		return null;
	}
	
	@Override
	protected AbstractLink getNewLink(String id) {
		
		Link<?> link = new Link<Void>(id) {
			public void onClick() {
				try {
					MenuItemPanelV5.this.onClick();
				}
				catch (Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
		};
		
		if (getPopupSettings()!=null) {
			link.setPopupSettings(getPopupSettings());
		}
		
		if (getTarget()!=null) {
			link.add(new AttributeModifier("target", getTarget()));
		}
		
		if (getOpener()) {
			link.add(new AttributeAppender("rel", "opener"));
		}
		
		return link;
	}
}
