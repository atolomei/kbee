package com.novamens.logging;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ApplicationRestartEvent")
public class ApplicationRestartEvent extends SystemEvent {

	public ApplicationRestartEvent(String message) {
		super();
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setAuditSet(AuditSet.SYSTEM);
	}

	public ApplicationRestartEvent() {
		super();
		setAuditSet(AuditSet.SYSTEM);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
	
	
