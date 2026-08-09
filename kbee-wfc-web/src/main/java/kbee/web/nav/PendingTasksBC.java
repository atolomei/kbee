package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class PendingTasksBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public PendingTasksBC() {
		super("bc.pendingtasks");
	}
	
	@Override
	public void onClick() {
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("task-pending-page"));
	}
}
