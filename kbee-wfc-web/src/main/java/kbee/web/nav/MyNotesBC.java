package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;


public class MyNotesBC extends BCElement {

	private static final long serialVersionUID = 1L;

	public  MyNotesBC() {
		super("bc.my-notes");
	}
	
	@Override
	public void onClick() {

		// setResponsePage(new UserNotesPage());
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-notes-page"));
	}
}
