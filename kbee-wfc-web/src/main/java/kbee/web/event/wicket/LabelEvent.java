package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

						
public class LabelEvent extends AbstractWicketAjaxEvent {
	public LabelEvent(AjaxRequestTarget target) {
		super(target);
	}
}
