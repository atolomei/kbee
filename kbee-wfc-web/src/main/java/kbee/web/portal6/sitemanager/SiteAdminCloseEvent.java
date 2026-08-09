package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.PortalObject;

public class SiteAdminCloseEvent<T extends PortalObject> extends SiteAdminEvent<T> {

	public SiteAdminCloseEvent(AjaxRequestTarget requestTarget, IModel<T> model) {
		super(requestTarget, model);
	}

	
	
}
