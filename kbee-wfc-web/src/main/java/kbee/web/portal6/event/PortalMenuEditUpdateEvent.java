package kbee.web.portal6.event;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalPersistentMenu;

public class PortalMenuEditUpdateEvent extends PortalAjaxEvent<PortalPersistentMenu> {
			
	private static final long serialVersionUID = 1L;

	//private boolean isediting = false; 
	
	public PortalMenuEditUpdateEvent(AjaxRequestTarget requestTarget, IModel<PortalPersistentMenu> model) {
		super(requestTarget, model);
		//this.isediting=isediting;
	}
	
	//public boolean isEditing() {
	//	return this.isediting;
	//}
}
