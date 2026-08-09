package kbee.web.nav;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import com.novamens.wicket.util.BCElement;

import kbee.web.service.ApplicationSiteMapService;

public class GeneralSettingsBC extends BCElement {
	private static final long serialVersionUID = 1L;

	public GeneralSettingsBC() {
		super("bc.generalsettings");
	}
	
	@Override
	public void onClick() {
		PageParameters pa= new PageParameters();
		pa.add("id", getDomain().getId().toString());
		setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-generalsettings-page", pa));
	}

	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
}
