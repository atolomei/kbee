package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class AuditTrailEvent extends AbstractWicketAjaxEvent {
				
	public AuditTrailEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
}
