package com.novamens.kbee.domain.lifecycle;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.DomainLifeCycleService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.domain.DomainSettingsService;

import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.kbee.content.service.KbeeDomainLifeCycleService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

public class DomainDefaultBuilder {

				
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainDefaultBuilder.class.getName());
	
	static private final String ORGANIZATION 		= "Novamens";
	
	DomainLifeCycleService service;
	
	public DomainDefaultBuilder(DomainLifeCycleService service) {
		this.service=service;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Domain build() throws ContentMgmtException, ContentCreationException {
		
		Domain dom = getContentDao().findDomainByName(KbeeDomainLifeCycleService.DEFAULT_DOMAIN_NAME);
		
		if (dom!=null && dom.getDomainType()==DomainType.SYSTEM) {
			// throw new ContentMgmtException("Basic Template already exists");
			try {
				
				getDomainLifeCycleService().wipe(dom);
				
			} catch (ServiceNotFoundException e) {
				logger.error(e);
			}
		}
		
		return dom;
	}
	

	/** -----------------------------------------------------------------------------------
	 */
	private DomainLifeCycleService getDomainLifeCycleService() {
		return this.service;
				
	}
	
	/** -----------------------------------------------------------------------------------
	 *
	 * 
	 */
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		return dao;
	}
	
}
