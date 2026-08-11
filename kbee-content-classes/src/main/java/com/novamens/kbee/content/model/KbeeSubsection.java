package com.novamens.kbee.content.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.Subsection;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public class KbeeSubsection implements Subsection, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	public KbeeSubsection(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name ;
	}
	
	public Multiplicity getMultiplicity() {
		return null;
	}

	public boolean isVisible(String context) {
		return true;
	}
	
	public void setVisibility(String context, boolean value) {
		
	}
	
	public boolean isOrdered() {
		return true;
	}
	
	public Domain getDomain() {
		return null;
	}
	
	public void setDomain(Domain domain) {
		
	}
	
	public Serializable getId() {
		return null;
	}
	
	public String getAlias() {
		return null;
	}
	
	
	public void setId(Serializable id) {
		
	}
	
	public String getDisplayName() {
		return name;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return null;
	}
	
	public void setState(ObjectState enabled) {
		
	}
	
	public ObjectState getState() {
		return null;
	}
	
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		return null;
	}
	
	public String getCreationOffsetDateTimeColloquial() {
		return null;
	}
	
	public void setDefaultAudit() {
		
	}

	public void setLastModifiedUser(User user) {
		
	}
	
	public User getLastModifiedUser() {
		return null;
	}
	
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		
	}
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime(String css) {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isDefaultStructure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOnlyRootEdit() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAlias(String alias) {
		// TODO Auto-generated method stub
		
	}
}
