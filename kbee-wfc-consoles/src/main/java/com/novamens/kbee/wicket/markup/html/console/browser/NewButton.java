package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public abstract class NewButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	public NewButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			addLink();
	}
	
	protected void addLink() {
		
		Link<Void> link = new Link<Void>("link") {
			@Override
			public void onClick() {
				NewButton.this.onClick();
			}
			@Override
			public boolean isEnabled() {
				return NewButton.this.isEnabled();
			}
			@Override
			public boolean isVisible() {
				return NewButton.this.isVisible();
			}
		};
		
		
		if (getButtonCss()!=null)
			link.add(new AttributeModifier("class", getButtonCss()));
		
			
		Label label = new Label("label", getLabel());		

		link.add(label);

		if (getTarget()!=null) {
			link.add(new AttributeModifier("target", getTarget()));
		}
		
		add(link);
	}
	
	protected String getButtonCss() {
		return null;
	}

	protected IModel<String> getLabel() {
		return new StringResourceModel("new", this, null);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	public String getTarget() {
		return null;
	}
	
	public abstract void onClick();
}
