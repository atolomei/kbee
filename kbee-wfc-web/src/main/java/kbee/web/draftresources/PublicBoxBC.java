package kbee.web.draftresources;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class PublicBoxBC extends BCElement {
				
private static final long serialVersionUID = 1L;
	
	public  PublicBoxBC() {
		super("draft-folder-public");
	}
	
	@Override
	public void onClick() {
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("resources-publicbox-page"));

	}
}
