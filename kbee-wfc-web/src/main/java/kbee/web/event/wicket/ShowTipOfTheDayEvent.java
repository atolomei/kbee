package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ShowTipOfTheDayEvent extends AbstractWicketAjaxEvent {

	public ShowTipOfTheDayEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);

	}
}
