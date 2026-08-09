package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class FacetsBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public FacetsBC() {
		super("bc.facets");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-facets-page"));
	}
}
