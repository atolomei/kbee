package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;


import com.novamens.portal6.model.PortalPersistentMenu;

public class PortalMenuEditEvent extends PortalAjaxEvent<PortalPersistentMenu> {

	private static final long serialVersionUID = 1L;

	private boolean isediting = false; 
	
	public PortalMenuEditEvent(AjaxRequestTarget requestTarget, IModel<PortalPersistentMenu> model, boolean isediting) {
		super(requestTarget, model);
		this.isediting=isediting;
	}
	
	public boolean isEditing() {
		return this.isediting;
	}

}
