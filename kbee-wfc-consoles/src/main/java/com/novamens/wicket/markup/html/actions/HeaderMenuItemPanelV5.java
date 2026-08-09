package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;


@SuppressWarnings("serial")
public abstract class HeaderMenuItemPanelV5<T> extends AbstractMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;

	public HeaderMenuItemPanelV5(String id) {
		this(id, null);
	}
	
	public HeaderMenuItemPanelV5(String id, final String iconcss) {
		super(id, iconcss);
		
		WebMarkupContainer icon = new WebMarkupContainer ("item-icon") {
			public boolean isVisible() {
				return HeaderMenuItemPanelV5.this.isIcon(); 
			}
		};
		
		
		if (iconcss!=null) {
			icon.add(new AttributeModifier("class", getIconCssClass()));
		}
		
		icon.add(new AttributeModifier("data-original-title", new Model<String>() {
			public String getObject() {
				return getLabel();
			}
		}));
		
		add(icon);
		
		Label label = new Label("item-label", new Model<String>() {
			public String getObject() {
				return getLabel();
			}
		});
		if (isEscapeModelString()) 
			label.setEscapeModelStrings(true);
		else
			label.setEscapeModelStrings(false);
		
		add(label);
	}
	

	protected boolean isIcon() {
		return false;
	}

	@Override
	public void onClick() throws Exception {
		
	}
	
	@Override
	public String getBeforeClick() {
		return null;
	}
	
	@Override
	public String getCssClass() {
		return "section-header";
	}

	protected boolean isEscapeModelString() {
		return false;
	}
} 