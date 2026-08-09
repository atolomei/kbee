package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class DomainRecyleBinBC extends BCElement {
			
	private static final long serialVersionUID = 1L;

	public DomainRecyleBinBC () {
		super("mainmenu.domains-recycle-bin");
	}
	
	/**
	 * 
	 */
	@Override
	public void onClick() {
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-domain-recycle-bin-page"));
	}
}
