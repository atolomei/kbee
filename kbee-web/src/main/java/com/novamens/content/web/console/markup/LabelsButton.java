package com.novamens.content.web.console.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.user.UserLabel;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;

import kbee.web.console.BaseBrowser;
import kbee.web.event.wicket.LabelEvent;

public abstract class LabelsButton extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;


	private IModel<String> icon_css = default_icon;
	static IModel<String> default_icon = new Model<String> ("far fa-tag fa-fw");
	

	
	public LabelsButton(BaseBrowser<?> browser, boolean isicon) {
		this (browser, Align.TOP_NONE, isicon);
	}
	
	
	public LabelsButton(BaseBrowser<?> browser, Align align) {
			this (browser, align, false);
	}
	
	public LabelsButton(BaseBrowser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);

		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(LabelsButton.this);
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
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				LabelsButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return LabelsButton.this.isEnabled();
			}
		};
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			private static final long serialVersionUID = 1L;
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
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	
	protected IModel<String> getIconCss() {
		return icon_css; 
	}
	
	public abstract void onClick(AjaxRequestTarget target);
}
