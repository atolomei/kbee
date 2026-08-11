package kbee.web.notes;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class WorkNotesGridUpdate extends AbstractWicketAjaxEvent {
			
	public WorkNotesGridUpdate(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}
}
