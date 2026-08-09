package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class PortalAjaxShowPayloadEvent<T extends PortalObject> extends PortalAjaxEvent<T> {

	
private static final long serialVersionUID = 1L;
	
	int v=PortalAjaxEvent.SHOW_PAYLOAD_NO;
	 
	public PortalAjaxShowPayloadEvent(AjaxRequestTarget requestTarget, IModel<T> model, int show) {
		super(requestTarget, model);
		 this.v= show;
	}
	
	public int getShowPayloadMode() {
		return this.v;
	}
	
}
