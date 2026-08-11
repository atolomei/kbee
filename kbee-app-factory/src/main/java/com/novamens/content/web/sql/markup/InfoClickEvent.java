package com.novamens.content.web.sql.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class InfoClickEvent extends AbstractWicketAjaxEvent {

	public InfoClickEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
