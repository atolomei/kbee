package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

import kbee.web.portal6.event.PortalAjaxEvent;

public class PortalAjaxEditorDispositionEvent<T extends PortalObject> extends PortalAjaxEvent<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int value;
	public PortalAjaxEditorDispositionEvent(AjaxRequestTarget requestTarget, IModel<T> model, int value) {
		super(requestTarget, model);
		this.value=value;
	}
	
	public int getDisposition() {
		return value;
	}

}
