package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;


/**
 *
 *
 */
public class ClickResetPasswordEvent extends AbstractWicketAjaxEvent {

	public ClickResetPasswordEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
	
	
	
}
