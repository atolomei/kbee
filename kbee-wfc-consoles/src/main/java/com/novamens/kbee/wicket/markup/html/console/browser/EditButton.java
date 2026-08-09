package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;


public abstract class EditButton extends ToolbarItem {
	
	private static final long serialVersionUID = 1L;

	private IModel<String> icon_css = default_icon;
	static IModel<String> default_icon = new Model<String> ("far fa-edit fa-fw");
	

	public EditButton(BaseBrowser<?> browser, boolean isicon) {
		this(browser, Align.TOP_NONE, isicon);
	}
	
	public EditButton(BaseBrowser<?> browser) {
		this(browser, Align.TOP_LEFT);
		
	}
	
	public EditButton(BaseBrowser<?> browser, Align al) {
		this(browser, al, false);
	}
	
	
	public EditButton(BaseBrowser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		setOutputMarkupId(true);
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(EditButton.this);
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("link")==null) {
			addLink();
		}
	}
	
	
	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				EditButton.this.onClick(target);
			}
			
			@Override
			public boolean isEnabled() {
				return EditButton.this.isEnabled();
			}
		};
		
		Label label = new Label("label", getLabel());		

		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return isIcon() && getIconCss()!=null;
			}
		};
		
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss()));
				
		link.add(icon);
		link.add(label);
		add(link);
		
	}

	
	protected IModel<String> getIconCss() {
		return icon_css; 
	}

	protected IModel<String> getLabel() {
		return new StringResourceModel("label", this, null);
	}

	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	public abstract void onClick(AjaxRequestTarget target);
}
