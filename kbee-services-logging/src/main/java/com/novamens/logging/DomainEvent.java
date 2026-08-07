package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.ObjectId;
import com.novamens.dom.Domain;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("DomainEvent")
public abstract class DomainEvent extends AbstractObjectEvent {
				
	 
	
	public DomainEvent() {
		super();
		setAuditSet(AuditSet.MODEL);
	}
	
	public DomainEvent(Domain domain, List<String> updatedParts) {
		super();
		setDomain(domain);
		setParameters(getDescription(updatedParts));
	}
	
	public void setDomain(Domain domain) {
		setObjectId((new ObjectId(domain)).toString());
		setKbeeClass(domain.getName());
		super.setDomain(domain);
		setDomainId((Long)domain.getId());
		setTitle(domain.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	
	public DomainEvent(Domain domain, String description) {
		super();
		setDomain(domain);
		setParameters(description);
	}
	
	
	@Deprecated
	@Override
	public String getEventType() {
		return "Domain";
	}
	
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	
	public String getTarget() {
		if (getDomain()!=null)
			return "id: " + String.valueOf(getDomain().getId())+". " + getDomain().getDisplayName();
		return "na";
	}
	
	@Override
	public String getAction() {
		return "DomainAction";
	}

	/***
	 * Replaces getEventType
	 */
	@Override
	public String getType() {
		return "Domain";
	}
	
	@Override
	public String getObjectClass() {
		return "Domain";  
	}
	
}
