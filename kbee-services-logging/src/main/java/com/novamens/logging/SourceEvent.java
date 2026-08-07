package com.novamens.logging;

import com.novamens.content.base.Source;
import com.novamens.content.model.ObjectId;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue("SourceEvent")
public class SourceEvent extends AbstractObjectEvent {

	public SourceEvent() {
		super();
		setAuditSet(AuditSet.SYSTEM);
	}

	public SourceEvent(Source source, String part) {
		super();
		setAuditSet(AuditSet.SYSTEM);
		setSource(source);
		setParameters(part);
	}
	
	
	public SourceEvent(Source source, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.SYSTEM);
		setSource(source);
		setParameters(getDescription(updatedParts));
	}
	
	public void setSource(Source source) {
		setObjectId((new ObjectId(source)).toString());
		setKbeeClass("Source");
		setDomainId((Long)source.getDomain().getId());
		setTitle(source.getDisplayName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Deprecated
	@Override
	public String getEventType() {
		return "System";
	}
	
	@Override
	public String getAction() {
		return getEventType();
	}
	
	@Override
	public String getType() {
		return "System";
	}
	
	@Override
	public String getTarget() {
		return getKbeeClass() + " - "  + getObjectId()!=null? getObjectId().toString() : "[null]";
	}
	
	@Override
	public String getObjectClass() {
		return getKbeeClass(); 
	}
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
}
