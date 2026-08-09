package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.event.Event;
import com.novamens.portal6.model.PortalObject;

import kbee.web.portal6.event.PortalAjaxEvent;

public class PortalAjaxPinEditorEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int pin_editor;
	public PortalAjaxPinEditorEvent(AjaxRequestTarget requestTarget, IModel<T> model, int pin_editor) {
		super(requestTarget, model);
		this.pin_editor = pin_editor;
	}

	
	public int getPinEditor() {
		return this.pin_editor;
	}
	
}
