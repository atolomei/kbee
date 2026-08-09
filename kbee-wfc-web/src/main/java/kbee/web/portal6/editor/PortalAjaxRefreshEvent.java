package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.event.Event;
import com.novamens.portal6.model.PortalObject;

import kbee.web.portal6.event.PortalAjaxEvent;

public class PortalAjaxRefreshEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements Event {

	private static final long serialVersionUID = 1L;

	public PortalAjaxRefreshEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget, model);
	}

}
