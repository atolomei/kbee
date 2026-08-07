package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("ObjectEvent")
public class ObjectEvent<T extends com.novamens.dom.Object> extends AbstractObjectEvent {
	
	public ObjectEvent() {
		super();
		setAuditSet(AuditSet.SYSTEM);
	}
	
	public ObjectEvent(T object, String part) {
		super();
		setAuditSet(object.getAuditSet());
		setObject(object);
		setParameters(part);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public ObjectEvent(T object, List<String> updatedParts) {
		super();
		setAuditSet(object.getAuditSet());
		setObject(object);
		setParameters(getDescription(updatedParts));
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	@Deprecated
	public String getEventType() {
		return getAuditSet().getDisplayName();
	}
	
	@Override
	public String getAction() {
		return getEventType();
	}
	
	@Override
	public String getType() {
		return getAuditSet().getDisplayName();
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