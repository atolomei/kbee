package kbee.web.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class UsersBatchSetRoleButtonEvent extends AbstractWicketAjaxEvent implements Event {

	public UsersBatchSetRoleButtonEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
