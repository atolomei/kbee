package kbee.web.searcher;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.searcher.page.SearcherHomePage;
import kbee.web.service.ApplicationSiteMapService;

public class PortalBC extends BCElement {

	private IModel<Site> model;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public PortalBC(IModel<Site> model) {
		super("home");
		this.model=model;
		
	}
	
	public void onClick() {
		//setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage));
		setResponsePage(new SearcherHomePage(getSiteModel()));
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	public IModel<Site> getSiteModel() {
		return model;
	}
	
	
	
}
