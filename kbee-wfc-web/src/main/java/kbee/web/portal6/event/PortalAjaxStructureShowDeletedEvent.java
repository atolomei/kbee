package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class PortalAjaxStructureShowDeletedEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable {

	
	private static final long serialVersionUID = 1L;
	
	int value = PortalAjaxEvent.SHOW_DELETED_NO;
	
	public PortalAjaxStructureShowDeletedEvent(AjaxRequestTarget requestTarget, IModel<T> model, int value) {
		super(requestTarget, model);
		this.value=value;
	
	}
	
	
	public int getShowDeleted() {
		return this.value;
	}

}
