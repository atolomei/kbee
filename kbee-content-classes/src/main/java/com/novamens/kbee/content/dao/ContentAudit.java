package com.novamens.kbee.content.dao;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

@Deprecated 
public class ContentAudit {
	static KbeeContentDao dao = (KbeeContentDao)  ServiceLocator.getService(BeansService.class).getBean("contentDao");
	
	//No me queda claro el fin de esta clase
	
	@PreUpdate
	@PrePersist
	public void setLastModified(com.novamens.dom.Object object) {
		
		throw new RuntimeException ("This class is Deprecated");
		
		//if (object.getLastModifiedDate()==null)
		//	object.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		//	if (object.getLastModifiedUser()==null)
		//		object.setLastModifiedUser(dao.getSessionUser());
		//	if (object.getState()==null)
		//		object.setState(ObjectState.ENABLED);
		//	if (object.getDomain()==null)
		//		object.setDomain(dao.getDomain());
	}
}
