package com.novamens.kbee.wicket.markup.html.console.browser;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.Browser;

public abstract class DeleteButton extends ToolbarItem {
	
	private static final long serialVersionUID = 1L;

	private IModel<String> icon_css = default_icon;
	static IModel<String> default_icon = new Model<String> ("far fa-trash-alt fa-fw");
	

	
	public DeleteButton(Browser<?> browser, boolean icon) {
		this(browser, Align.TOP_NONE, false);
	}	
	
	public DeleteButton(Browser<?> browser, Align align) {
		this(browser, align, false);
	}
	
	public DeleteButton(Browser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		
		
		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(DeleteButton.this);
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("link")==null)
			addLink();
	}
	
	protected IModel<String> getIconCss() {
		return icon_css; 
	}

	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				DeleteButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return DeleteButton.this.isEnabled();
			}
		};
		
		
		if (getAnchorTitle()!=null)
			link.add(new AttributeModifier("title", getAnchorTitle()));
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return isIcon() && (getIconCss()!=null);
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
	
	public abstract void onClick(AjaxRequestTarget target);
	
}
