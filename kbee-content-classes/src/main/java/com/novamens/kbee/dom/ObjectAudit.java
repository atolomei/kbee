package com.novamens.kbee.dom;


import java.time.OffsetDateTime;


import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.novamens.beans.BeansService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.dao.KbeeContentDao;
import com.novamens.service.ServiceLocator;

public class ObjectAudit {
	static KbeeContentDao dao = (KbeeContentDao)  ServiceLocator.getService(BeansService.class).getBean("contentDao");
	
	@PreUpdate
	@PrePersist
	public void setLastModified(com.novamens.dom.Object object) {
		
		if (object.getLastModifiedOffsetDateTime()==null)
				object.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				
		if (object.getLastModifiedUser()==null)
			object.setLastModifiedUser(dao.getSessionUser());
		
		if (object.getState()==null)
			object.setState(ObjectState.ENABLED);
		
		 
	}
}
