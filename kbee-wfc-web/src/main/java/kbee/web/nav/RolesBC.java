package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class RolesBC extends BCElement {
	private static final long serialVersionUID = 1L;
	
	public RolesBC() {
		super("bc.roles");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-roles-page"));
	}
}
