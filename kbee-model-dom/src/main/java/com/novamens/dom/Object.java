package com.novamens.dom;

import java.io.Serializable;

import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public interface Object extends Identifiable, Auditable {
	
	public void setId(Serializable id);
	
	public String getName();
	
	<T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException;
	
	public void setState(ObjectState enabled);
	public ObjectState getState();
}