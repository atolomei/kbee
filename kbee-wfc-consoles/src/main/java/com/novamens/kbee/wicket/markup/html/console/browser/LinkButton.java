package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public abstract class LinkButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	
	static IModel<String> default_icon 		= new Model<String> ("far fa-tasks fa-fw");
	static IModel<String> default_link_css 	= new Model<String> ("btn-md btn btn-default");
									

	private IModel<String> icon_css = default_icon;
	private IModel<String> label;
	private IModel<String> link_css = default_link_css;
	
	
	public LinkButton(BaseBrowser<?> browser, boolean isicon) {
		this (browser, Align.TOP_NONE, isicon, null);
	}
	
	public LinkButton(BaseBrowser<?> browser, Align align) {
			this (browser, align, false, null);
	}

	public LinkButton(BaseBrowser<?> browser, Align align, IModel<String> label) {
		this (browser, align, false, label);
	}
	
	public LinkButton(BaseBrowser<?> browser, Align align, boolean isicon, IModel<String> label) {
		super(browser, align, isicon);

		setOutputMarkupId(true);
		setLabel(label);
		
		add(new WicketEventListener<SelectionEvent>() {
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(LinkButton.this);
			}
		});
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			addLink();
	}
	
	public void setLabel(IModel<String> label) {
		this.label = label;
	}
	
	public IModel<String> getLabel() {
		return this.label;
	}
	
	public String getTarget() {
		return null;
	}
	
	
	public void setLinkCss(IModel<String> label) {
		this.link_css = label;
	}
	
	public IModel<String> getLinkCss() {
		return this.link_css;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	
	public IModel<String> getIconCss() {
		return icon_css; 
	}
	
	public abstract void onClick();

	
	protected void addLink() {
		
		Link<Void> link = new Link<Void>("link") {
			@Override
			public void onClick() {
				LinkButton.this.onClick();
			}
			@Override
			public boolean isEnabled() {
				return LinkButton.this.isEnabled();
			}
		};

		
		if (getTarget()!=null)
			link.add(new AttributeModifier("target", getTarget()));
		
		if (getLinkCss()!=null)
			link.add(new AttributeModifier("class", getLinkCss()));
		
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			public boolean isVisible() {
				return isIcon() && getIconCss()!=null;
			}
		};
		
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss()));
		 
		
		Label la= new Label("label", (getLabel()!=null?getLabel().getObject():"")) {
			public boolean isVisible() {
				return getLabel() !=null;
			}
		};
		
		link.add(la);
		link.add(icon);
		
		add(link);
	}
}
