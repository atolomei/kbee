package kbee.web.nav;



import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class SystemInfoBC extends BCElement {
				
	private static final long serialVersionUID = 1L;

	public SystemInfoBC() {
		super("bc.systeminfo");
	}
	
	@Override
	public void onClick() {

		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-system-info-page"));
	}
}
