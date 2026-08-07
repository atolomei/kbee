package com.novamens.logging;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.DomainProxy;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("DownloadEvent")
public class DownloadEvent extends AbstractLogEvent implements DomainObject {
	
	@Column(name = "EVENT_TITLE")
	private String title;
	
	@Column(name = "EVENT_DOMAIN_ID")
	private Long domainId;
	
	public DownloadEvent() {
		super();
	}
	
	public DownloadEvent(String context, String filename) {
		super();
		setTitle(context);
		setParameters(filename);
		 
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setDomain(((KbeeUser)getEventUser()).getDomain());
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
	public Domain getDomain() {
		return new DomainProxy(domainId);
	}

	public void setDomain(Domain domain) {
		if (domain!=null)
		setDomainId((Long)domain.getId());
	}
	
	@Override
	public String getType() {
		return "Download";
	}
	
	@Override
	public String getAction() {
		return "Download";
	}

}
