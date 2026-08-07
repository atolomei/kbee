package com.novamens.content.web.console.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;

public abstract class TakeTasksButton extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;

	public TakeTasksButton(BaseBrowser<Content> browser, Align align) {
		super(browser, align);
		
		setOutputMarkupId(true);
		
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(TakeTasksButton.this);
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
		add(new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				TakeTasksButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return TakeTasksButton.this.isEnabled();
			}
		});
	}

	@Override
	public boolean isEnabled() {
		
		if (super.getBrowser().getSelection().isEmpty())
			return false;
		
		ContentSystemSecurityService se=ServiceLocator.getService(ContentSystemSecurityService.class);
		
		for (IModel<?> model: super.getBrowser().getSelection()) {
			if (!se.isTakeable(((Content) model.getObject())))
				return false;
		}
		return true;
	}
	
	public abstract void onClick(AjaxRequestTarget target);

}
