package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;



public class BillboardBC extends BCElement {

	private static final long serialVersionUID = 1L;
	
	public BillboardBC() {
		super("bc.worknotes");
	}
	
	@Override
	public void onClick() {
		// setResponsePage(new BillboardsPage());
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("alert-management-billboards-page"));
	}
}
