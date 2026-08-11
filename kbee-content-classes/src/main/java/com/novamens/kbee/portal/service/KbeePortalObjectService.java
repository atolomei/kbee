package com.novamens.kbee.portal.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.user.UserService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.service.PortalObjectService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class KbeePortalObjectService implements PortalObjectService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObjectService.class.getName());
	
	
	static private Logger txLogger = LogManager.getLogger("TxLogger");
	
	private PortalObject po;
	private PortalDao dao;

	
	public KbeePortalObjectService() {
		
	}
	
	
	public KbeePortalObjectService(PortalObject po) {
		this.po=po;
	}
	
	
	
	public PortalObject getObject() {
		return this.po;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete() 										throws ContentMgmtException {

		getPortalDao().delete(getObject());
		
		// see log4j2.xml configuration
		//RemoveEvent event = new RemoveEvent(getContent());
		//event.setParameters(parameter);
		//txLogger.info(event);
		// Spring
		//ServiceLocator.getService(EventService.class).fire(new AppDeleteEvent(getContent()));
		
		
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void save() 										throws ContentMgmtException{
		getPortalDao().save(getObject());
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void archive()								throws ContentMgmtException {
		getObject().setState(ObjectState.ARCHIVED);
		save();
		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void unArchive() 							throws ContentMgmtException{
		getObject().setState(ObjectState.ENABLED);
		save();

		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void recycle() 								throws ContentMgmtException{
		getObject().setState(ObjectState.ENABLED);
		save();
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void restore() 								throws ContentMgmtException{
		getObject().setState(ObjectState.ENABLED);
		save();
		
	}

	
	public void setPortalDao(PortalDao dao) {
		this.dao = dao;
	}

	public PortalDao getPortalDao() {
		return this.dao;
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}

	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	protected SecurityDao getSecurityDao() {
		return (SecurityDao) ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

}
