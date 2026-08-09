package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class MyBoxBC extends BCElement {


	private static final long serialVersionUID = 1L;
	
	public  MyBoxBC() {
		super("bc.mybox");
	}
	
	@Override
	public void onClick() {
		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("resources-mybox-page"));

	}
	
}
