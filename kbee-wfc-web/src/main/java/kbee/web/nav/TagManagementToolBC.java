package kbee.web.nav;


import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import kbee.web.service.ApplicationSiteMapService;

public class TagManagementToolBC extends BCElement {
			
	private static final long serialVersionUID = 1L;

	public TagManagementToolBC() {
		super("bc.tagmanagementtool");
	}
	
	@Override
	public void onClick() {
	    setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("data-management-tagtool-page"));
	}
}
