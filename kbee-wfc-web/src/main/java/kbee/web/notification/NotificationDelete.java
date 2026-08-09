package kbee.web.notification;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
				

public class NotificationDelete extends AbstractWicketAjaxEvent {
	public NotificationDelete(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
}
