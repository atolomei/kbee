package kbee.web.nav;


import org.apache.wicket.model.IModel;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class GroupsBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public GroupsBC() {
		super("bc.groups");
	}
	
	
	public GroupsBC(IModel<String> title) {
		super(title);
	}

	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-groups-page"));
	}
}
