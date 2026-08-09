package kbee.web.nav;

import org.apache.wicket.markup.html.WebPage;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class MyDocumentsBC extends BCElement {
			
	private static final long serialVersionUID = 1L;

	public MyDocumentsBC() {
		super("mydocuments");
	}
	
	@Override
	public void onClick() {
		WebPage page = ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.MyDocumentsPage);
		setResponsePage(page);
	}
	
}
