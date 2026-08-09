package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class SignEvent extends AbstractWicketAjaxEvent {

	
	boolean is_sign = true;
	
public SignEvent(AjaxRequestTarget requestTarget, boolean isSign) {
		super(requestTarget);
		this.is_sign=isSign;
	}

	public boolean isSign() {
		return this.is_sign;
	}
}
