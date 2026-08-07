package com.novamens.content.web.security.markup;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.novamens.content.entity.Person;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import kbee.web.console.BaseBrowser;
import kbee.web.event.wicket.ClickResetPasswordEvent;

public class UsersBatchPasswordChangeButton extends ToolbarItem {
			
	private static final long serialVersionUID = 1L;

	public UsersBatchPasswordChangeButton(BaseBrowser<Person> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		setOutputMarkupId(true);
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(UsersBatchPasswordChangeButton.this);
			}
		});
	}

	public void close(AjaxRequestTarget target) {
		target.add(getPage());
	}

	
	@Override
	public boolean isEnabled() {
		return !super.getBrowser().getSelection().isEmpty();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("pwd-change-modal")==null) {
			add(new AjaxLink<Void>("link") {
				private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
					fire( new ClickResetPasswordEvent(target));
				}
			});
		}
	}
}
