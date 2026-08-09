package kbee.web.portal6.sitemanager;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;

import kbee.web.nav.NavigationPanel;

public class PortalNavigationBar extends NavigationPanel<Site> {

	private static final long serialVersionUID = 1L;
	
	IModel<Site> model;
	public PortalNavigationBar(String id, IModel<Site> model) {
		super(id);
		this.model=model;
	}
	@Override
	public void navigate() {
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	public void onDetach() {
		if (this.model!=null)
			this.model.detach();
		super.onDetach();
	}
	
	@Override
	public boolean isFromContentBase() {
		return false;
	}
	
	

}
