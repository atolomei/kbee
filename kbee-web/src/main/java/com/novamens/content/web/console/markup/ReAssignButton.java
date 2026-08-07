package com.novamens.content.web.console.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;

public abstract class ReAssignButton extends ToolbarItem {
				
	private static final long serialVersionUID = 1L;

	public ReAssignButton(BaseBrowser<?> browser,boolean hasIcon) {
		this(browser, Align.TOP_NONE, hasIcon);
	}
	public ReAssignButton(BaseBrowser<?> browser, Align align) {
		this(browser, Align.TOP_NONE, false);
	}
	
	public ReAssignButton(BaseBrowser<?> browser, Align align, boolean hasIcon) {
		super(browser, align, hasIcon);
		
		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(ReAssignButton.this);
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

	protected IModel<String> getDefaultIcon() {
		return new Model<String> ("far fa-paper-plane fa-fw");
	}

	
	protected void addLink() {
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				ReAssignButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return ReAssignButton.this.isEnabled();
			}
		};
		
		Label label = new Label("label", getLabel());		

		link.add(label);
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return getIconCss()!=null && isIcon();
			}
		};
		
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss().getObject()));
		link.add(icon);
		
		
		
		link.add(label);
		add(link);
		
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
