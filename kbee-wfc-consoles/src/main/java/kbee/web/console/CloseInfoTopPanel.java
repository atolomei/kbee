package kbee.web.console;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class CloseInfoTopPanel extends AbstractWicketAjaxEvent {

	public CloseInfoTopPanel(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
}
