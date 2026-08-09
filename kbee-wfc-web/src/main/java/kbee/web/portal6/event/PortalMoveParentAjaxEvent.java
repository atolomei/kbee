package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.portal6.model.PortalObject;

public class PortalMoveParentAjaxEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable, WicketAjaxEvent {

	private static final long serialVersionUID = 1L;

	public PortalMoveParentAjaxEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget, model);
	
	}

}
