package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class LibrariesBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public LibrariesBC() {
		super("bc.libraries");
	}
	
	@Override
	public void onClick() {
		// 
		// setResponsePage(new LibrariesPage());
		
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-libraries-page"));
	}
}
