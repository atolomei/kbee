package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.portal6.model.PortalObject;
					
public class PortalArchiveAjaxEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable, WicketAjaxEvent {
				
	private static final long serialVersionUID = 1L;

	public PortalArchiveAjaxEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget, model);
	}

	@Override
	public void detach() {
		super.detach();
	}
}
