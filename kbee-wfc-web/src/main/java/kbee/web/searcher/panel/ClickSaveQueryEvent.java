package kbee.web.searcher.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ClickSaveQueryEvent extends AbstractWicketAjaxEvent {

	public ClickSaveQueryEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

}
