package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class SourcesBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public SourcesBC() {
		super("bc.sources");
	}
	
	@Override
	public void onClick() {
	
		//setResponsePage(new SourcesPage());
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-sources-page"));
	}
}
