package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class ContentBaseBC extends BCElement {

	private static final long serialVersionUID = 1L;

	public ContentBaseBC() {
		super("bc.contentbase");
	}
	
	@Override
	public void onClick() {
		
		//PageParameters pa= new PageParameters();
	    //pa.add("id", person.getId().toString());
	    
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("library-contentbase-page"));
	    
		// setResponsePage(new ContentBasePage());
	    
	}
}
