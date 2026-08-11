package com.novamens.kbee.content.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.portal6.model.Site;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("SiteEditEvent")
public class SiteDeleteEvent extends SiteEvent {

	public SiteDeleteEvent() {
	}

	public SiteDeleteEvent(Site site) {
		super(site);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}

}
