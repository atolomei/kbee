package com.novamens.kbee.content.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.portal6.model.Site;

/**
 *
 */
@Entity
@DiscriminatorValue("SiteUpdateEvent")
public class SiteUpdateEvent extends SiteEvent {

	public SiteUpdateEvent() {
	}

	public SiteUpdateEvent(Site site) {
		super(site);
	}

}
