package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;

@SuppressWarnings("serial")
public abstract class LinkMenuItemPanel<T> extends AbstractLinkMenuItemPanelV5<T> {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LinkMenuItemPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	public LinkMenuItemPanel(String id) {
		super(id, null);
	}
	
	public LinkMenuItemPanel(String id, IModel<T> model) {
		super(id, null);
		setModel(model);
	}
	
	
	public LinkMenuItemPanel(String id, IModel<T> model, int index) {
		super(id, null);
		setModel(model);
		setIndex(index);
	}
	
	public LinkMenuItemPanel(String id, String icon) {
		super(id, icon);
	}
	
	
	@Override
	protected AbstractLink getNewLink(String id) {
		
		Link<?> link = new Link<Void>(id) {
		
			public void onClick() {
				try {
					LinkMenuItemPanel.this.onClick();
				}
				catch (Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
			protected CharSequence getOnClickScript(final CharSequence url) {
				//return getBeforeClick();
				return null;
			}
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (getBeforeClick()!=null)
				tag.put("onclick", getBeforeClick());
				if (getTarget()!=null)
					tag.put("target", getTarget());
			}
		};
		return link;
	}
	
	public String getCssClass() {
		return null;
	}
	
	public String getBeforeClick() {
		return null;
	}
}
