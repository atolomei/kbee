package kbee.web.event.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class ClickSendByEmailEvent<T extends Content> extends AbstractWicketAjaxEvent {

	IModel<T> model;
	
	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}

	public ClickSendByEmailEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget);
		setModel(model);
	}
		

}
