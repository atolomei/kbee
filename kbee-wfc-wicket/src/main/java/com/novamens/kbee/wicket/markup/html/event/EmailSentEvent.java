package com.novamens.kbee.wicket.markup.html.event;

import java.time.Instant;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

public class EmailSentEvent<T> extends AbstractWicketAjaxEvent implements IDetachable {
 
	
	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
	
	public EmailSentEvent(IModel<T> model, AjaxRequestTarget target) {
		super(target);
		this.model=model;
	}

	@Override
	public void detach() {
			if (model!=null)
				model.detach();
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	
	@Override
	public Instant getTime() {
		return Instant.now();
	}

	@Override
	public Object getObject() {
		if (model!=null)
			return model.getObject();
		return null;
	}

}
