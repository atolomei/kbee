package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ErrorEvent<T> extends AbstractWicketAjaxEvent implements IDetachable {

	private static final long serialVersionUID = 1L;
	
	private Throwable exception;
	private IModel<T> model;
	
	public ErrorEvent(AjaxRequestTarget requestTarget, IModel<T> model, Throwable e) {
		super(requestTarget);
		this.exception=e;
		this.model=model;
	}

	public ErrorEvent(AjaxRequestTarget requestTarget, Throwable e) {
		super(requestTarget);
		this.exception=e;
	}
	
	public ErrorEvent(AjaxRequestTarget requestTarget) {
		super(requestTarget);
	}

	public IModel<T> getModel() {
		return this.model;
	}
	
	public Throwable getThrowable() {
		return this.exception;
	}

	@Override
	public void detach() {
		
		if (this.model!=null)
			this.model.detach();
	}
	
}
