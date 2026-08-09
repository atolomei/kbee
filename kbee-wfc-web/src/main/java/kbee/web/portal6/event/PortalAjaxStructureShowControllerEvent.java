package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class PortalAjaxStructureShowControllerEvent<T extends PortalObject> extends PortalAjaxEvent<T> implements IDetachable {

	private static final long serialVersionUID = 1L;
	
	int editMode=PortalAjaxEvent.EDIT_MODE;
	 
	public PortalAjaxStructureShowControllerEvent(AjaxRequestTarget requestTarget, IModel<T> model, int editMode) {
		super(requestTarget, model);
		 this.editMode= editMode;
	}
	
	public int getEditMode() {
		return this.editMode;
	}

}
