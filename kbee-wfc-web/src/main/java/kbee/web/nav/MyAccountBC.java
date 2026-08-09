package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class MyAccountBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public MyAccountBC() {
		super("bc.myaccount");
	}
	
	@Override
	public void onClick() {

		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page"));
	}
}
