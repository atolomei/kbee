package com.novamens.kbee.content.webapi.type;

import com.novamens.dom.Domain;

import kbee.api.model.ISettings;

public class ISettingsAdapter implements Adapter<Domain, ISettings> {
	
	public ISettingsAdapter() {
	}
	
	public ISettings adapt(Domain domain) {
		
		ISettings settings = new ISettings();
		
		settings.setQuota(domain.getQuota());
		settings.setMaxUsers(domain.getMaxUsers());

		return settings;	
	}
}
