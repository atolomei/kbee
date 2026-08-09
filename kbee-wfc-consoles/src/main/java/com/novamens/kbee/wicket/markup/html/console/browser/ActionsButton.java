package com.novamens.kbee.wicket.markup.html.console.browser;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public abstract class ActionsButton extends ToolbarItem {
					
	private static final long serialVersionUID = 1L;
																					
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ActionsButton.class.getName());
	
	private IModel<String> icon_css = default_icon;
	
	static IModel<String> default_icon = new Model<String> ("far fa-tasks fa-fw");
	
	public ActionsButton(BaseBrowser<?> browser, boolean isicon) {
		this (browser, Align.TOP_NONE, isicon);
	}
	
	public ActionsButton(BaseBrowser<?> browser, Align align) {
		this (browser, align, false);
	}
	
	public ActionsButton(BaseBrowser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);

		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(ActionsButton.this);
			}
		});
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addLink();
	}
	
	protected void addLink() {
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ActionsButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return ActionsButton.this.isEnabled();
			}
		};
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			public boolean isVisible() {
				return isIcon() && getIconCss()!=null;
			}
		};
		
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss()));
				
		link.add(icon);
		add(link);
	}

	@Override
	public boolean isEnabled() {
		logger.debug(super.getBrowser().getSelection().isEmpty());
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	protected IModel<String> getIconCss() {
		return icon_css; 
	}
	
	public abstract void onClick(AjaxRequestTarget target);
}
