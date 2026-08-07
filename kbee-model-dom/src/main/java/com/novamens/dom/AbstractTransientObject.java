package com.novamens.dom;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;



//import com.novamens.content.model.Domain;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

 
// public class AbstractTransientObject implements Object, Indexable {

public class AbstractTransientObject implements Object {
	
	public Serializable getId() {
		return null;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	public void setState(ObjectState enabled) {
	}
	
	public void setLastModifiedDate(Date date) {
	}
	
	public void setLastModifiedUser(User user) {
	};
	
	public void setId(Serializable id) {
	}
	
	public ObjectState getState() {
		return null;
	}
	
	public User getLastModifiedUser() {
		return null;
	}
	
	public Date getLastModifiedDate() {
		return null;
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}

	public String getClassCode() {
		return this.getClass().getSimpleName();
	}
}
