package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;
			
public class CommandsBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public CommandsBC () {
		super("bc.commands");
	}
	
	@Override
	public void onClick() {
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("factory-commands-page"));
	}
}
