package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

import kbee.web.portal6.event.PortalAjaxEvent;

public class PortalCloseDataProviderAjaxEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable {

	private static final long serialVersionUID = 1L;

	public PortalCloseDataProviderAjaxEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget, model);
	}

	@Override
	public void detach() {
	}

}
