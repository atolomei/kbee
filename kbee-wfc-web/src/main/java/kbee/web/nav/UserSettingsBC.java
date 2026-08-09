package kbee.web.nav;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class UserSettingsBC extends BCElement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserSettingsBC() {
		super("bc.user-settings");
	}

	/**
	 * 
	 *  MyAccountPage
	 * @see {link KbeeApplicationSiteMapService}
	 */
	@Override
	public void onClick() {

		setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page"));
	}
}
