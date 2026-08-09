package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class RecycleBinBC extends BCElement {
				
	private static final long serialVersionUID = 1L;

	public RecycleBinBC() {
		super("bc.recyclebin");
	}
	
	@Override
	public void onClick() {
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("library-contentbase-page"));
	}
	
	
}
