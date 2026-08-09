package kbee.web.page;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ErrorPageEvent extends AbstractWicketAjaxEvent {
				
	
	Throwable exception;
	
	public ErrorPageEvent (AjaxRequestTarget requestTarget, Throwable e) {
		super(requestTarget);
		this.exception=e;
	}
	
	public ErrorPageEvent (AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

	
	public Throwable getThrowable() {
		return this.exception;
	}
	

}
