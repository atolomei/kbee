package com.novamens.kbee.wicket.markup.html.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;

public class DeleteContentEvent<T extends Content> extends AbstractWicketAjaxEvent implements IDetachable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IModel<T> model;
	
	public DeleteContentEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget);
		this.model=model;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	
	@Override
	public void detach() {
			if (model!=null)
				model.detach();
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append( this.getClass().getName());
		if (model!=null)
			str.append( " | " +model.getObject().getDisplayName());
		return str.toString();
		
	}
}
