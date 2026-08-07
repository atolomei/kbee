package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("ApplicationStartEvent")
public class ApplicationStartEvent extends SystemEvent {

	public ApplicationStartEvent (long mili) {
		super();
		setParameters(String.valueOf(mili));
		setAuditSet(AuditSet.SYSTEM);
	}

	public ApplicationStartEvent () {
		super();
		setAuditSet(AuditSet.SYSTEM);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
	
	
