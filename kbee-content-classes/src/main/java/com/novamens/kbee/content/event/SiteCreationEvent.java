package com.novamens.kbee.content.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.portal6.model.Site;
import com.novamens.security.User;

@Entity
@DiscriminatorValue("SiteCreationEvent")
public class SiteCreationEvent extends SiteEvent {

	static public String getClassEventType() {
		return "Create";
	}

	public SiteCreationEvent() {
	}

	public SiteCreationEvent(Site site) {
		super(site);
	}

	@Override
	public String getEventType() {
		return getClassEventType();
	}

	public SiteCreationEvent(Site site, User user) {
		super(site);
		setEventUser(user);
	}
}
