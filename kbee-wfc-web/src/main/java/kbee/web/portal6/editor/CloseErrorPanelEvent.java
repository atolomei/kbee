package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class CloseErrorPanelEvent<T> extends AbstractWicketAjaxEvent {

	public CloseErrorPanelEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);

	}

}
