package com.novamens.content.entity;

import java.util.List;

import com.novamens.dom.DomainObject;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public interface Entity extends com.novamens.dom.Object, DomainObject  {
	
	public String getDisplayName();
	
	public String getEmail();
	
	public <T extends Profile> T getProfile(Class<T> profileclass);
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException;
	
	public List<Profile> getProfiles();
	
	public default AuditSet getAuditSet() {
		return AuditSet.ENTITY;
	}
}