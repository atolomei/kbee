package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;


import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class RefreshClickEvent extends AbstractWicketAjaxEvent {
			
	public RefreshClickEvent(AjaxRequestTarget target) {
		super(target);
	}

}
