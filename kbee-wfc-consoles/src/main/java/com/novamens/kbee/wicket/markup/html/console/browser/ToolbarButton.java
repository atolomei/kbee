package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import kbee.web.console.BaseBrowser;

public abstract class ToolbarButton extends AbstractToolbarButton {
	
	private static final long serialVersionUID = 1L;

	public ToolbarButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}

	@Override
	protected void addLink() {
	
	Link<Void> link = new Link<Void> ("link") {
	
		private static final long serialVersionUID = 1L;
		@Override
		public void onClick() {
			ToolbarButton.this.onClick();
		}
		@Override
		public boolean isEnabled() {
			return ToolbarButton.this.isEnabled();
		}
	};

	
	
	if (getLinkCss()!=null)
		link.add(new AttributeModifier("class", getLinkCss()));
	
	WebMarkupContainer icon = new WebMarkupContainer("icon");		

		icon.add(new AttributeModifier("class", new Model<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public String getObject() {
				return getIcon();
			}
	}));
									
	Label label = new Label("label", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return getLabelStr();
			}
		}) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getLabelStr()!=null;
			}
	};

	link.add(icon);
	link.add(label);
	add(link);
	
	link.add(new AttributeModifier("title", new Model<String>() {
		private static final long serialVersionUID = 1L;
		@Override
		public String getObject() {
			if (getAnchorTitle()==null)
					return "";
			return getAnchorTitle();
		}
	}));
 }

 public abstract void onClick();


}
