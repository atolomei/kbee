package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class SiteDirectoryBC extends BCElement {
			
	private static final long serialVersionUID = 1L;

	public SiteDirectoryBC() {
		super("bc.site");
	}

	@Override
	public void onClick() {

		// sites-page
		//setResponsePage(new com.novamens.content.web.portal6.SitesPage());
	    //PageParameters pa= new PageParameters();
	    //pa.add("id", d_id);
		
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("sites-page"));
	    

	}
	
}
