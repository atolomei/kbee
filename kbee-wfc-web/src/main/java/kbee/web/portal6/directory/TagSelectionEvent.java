package kbee.web.portal6.directory;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class TagSelectionEvent extends AbstractWicketAjaxEvent {

	private String tag;

	public TagSelectionEvent(AjaxRequestTarget requestTarget, String tag) {
		super(requestTarget);
		this.tag = tag;
	}

	public String getTag() {
		return this.tag;
	}

}
