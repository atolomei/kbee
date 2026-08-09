package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;

public class MenuItemWithModelPanel<T> extends AbstractLinkMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MenuItemWithModelPanel.class.getName());
	
	IModel<T> model;

	public MenuItemWithModelPanel(String id, IModel<T> m) {
		this(id, m, null);
	}
	
	public MenuItemWithModelPanel(String id, IModel<T> m, String iconcss) {
		super(id, iconcss);
		this.model=m;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getBeforeClick() {
		return null;
	}
	
	public PopupSettings getPopupSettings() {
		return null;
	}
	
	public void onDetach() {
		super.onDetach();
		if (this.model!=null)
			this.model.detach();
	}
	
	@Override
	protected AbstractLink getNewLink(String id) {
		Link<?> link = new Link<Void>(id) {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				try {
					MenuItemWithModelPanel.this.onClick();
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
		return link;
	}

	@Override
	public void onClick() throws Exception {
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public IModel<T> getMenuItemModel() {
		return model;
	}
}