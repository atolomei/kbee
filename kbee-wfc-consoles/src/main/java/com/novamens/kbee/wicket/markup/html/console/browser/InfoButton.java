package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import kbee.web.console.BaseBrowser;


public abstract class InfoButton extends ToolbarItem {
		
	private static final long serialVersionUID = 1L;

	public InfoButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addLink();
	}
	
	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return InfoButton.this.isEnabled();
			}
			@Override
			public boolean isVisible() {
				return InfoButton.this.isVisible();
			}
		};
		
		add(link);
		
	}

	
	
	protected String getButtonCss() {
		return null;
	}

	protected IModel<String> getLabel() {
		return new StringResourceModel("info-label", this, null);
	}

	public abstract void onClick(AjaxRequestTarget target);

}
