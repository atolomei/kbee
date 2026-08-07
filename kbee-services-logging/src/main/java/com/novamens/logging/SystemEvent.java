package com.novamens.logging;

import javax.persistence.Entity;

import com.novamens.security.audit.AuditSet;

@Entity
public class SystemEvent extends AbstractLogEvent {

	
	public SystemEvent() {
		super();
		setAuditSet(AuditSet.SYSTEM);
	}
	
	@Override
	public String getEventType() {
		return "System";
	}
	
	@Override
	public String getType() {
		return "System";
	}
	
	@Override
	public String getAction() {
		return getEventType();
	}

	@Override
	public String toString() {
		return getAction();
	}
}
