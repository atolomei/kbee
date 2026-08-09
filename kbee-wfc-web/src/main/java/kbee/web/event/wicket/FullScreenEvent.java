package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class FullScreenEvent extends AbstractWicketAjaxEvent {

	public FullScreenEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
}
