package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

import kbee.web.portal6.event.PortalAjaxEvent;

public class PortalEditAjaxEnabled<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable {

	public PortalEditAjaxEnabled(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget, model);
	}

	
	public void detach() {
		super.detach();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
