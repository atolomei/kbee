package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.library.Library;
import com.novamens.content.model.ObjectId;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("LIbraryEvent")
public class LibraryEvent extends AbstractObjectEvent {
	
	public LibraryEvent() {
		super();
		setAuditSet(AuditSet.SYSTEM);
	}
	
	public LibraryEvent(Library cabinet, String part) {
		super();
		setAuditSet(AuditSet.SYSTEM);
		setCabinet(cabinet);
		setParameters(part);
	}
	
	public LibraryEvent(Library cabinet, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.SYSTEM);
		setCabinet(cabinet);
		setParameters(getDescription(updatedParts));
	}
	
	public void setCabinet(Library cabinet) {
		setObjectId((new ObjectId(cabinet)).toString());
		setKbeeClass("Library");
		setDomainId((Long)cabinet.getDomain().getId());
		setTitle(cabinet.getDisplayName());
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
