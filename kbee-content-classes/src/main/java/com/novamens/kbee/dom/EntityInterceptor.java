package com.novamens.kbee.dom;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.annotations.Type;

public class EntityInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;

	
	protected static final Log logger = LogFactory.getLog(EntityInterceptor.class);

	public boolean onSave(
				Object entity, 
				Serializable id, 
				Object[] state, 
				String[] propertyNames, 
				Type[] types) {
		

		if (entity instanceof com.novamens.security.Auditable) { 
			((com.novamens.security.Auditable) entity).setDefaultAudit();
		}
		
		//if (entity instanceof com.novamens.dom.Object) {
		//	logger.debug("After: " + ((com.novamens.dom.Object) entity).toString());
		//}
		
		return true;
	}
}
