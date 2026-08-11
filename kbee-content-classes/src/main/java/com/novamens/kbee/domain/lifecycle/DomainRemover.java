package com.novamens.kbee.domain.lifecycle;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.DataManagementException;
import com.novamens.content.service.DomainLifeCycleService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;

public class DomainRemover {

	DomainLifeCycleService service;

	public DomainRemover(DomainLifeCycleService service) {
		this.service=service;
	}
	
	
	/** -----------------------------------------------------------------------------------
	 */
	public void delete (Domain domain) throws DataManagementException {
		
		if (domain.getState()!=ObjectState.DELETED)
			throw new DataManagementException("Domain " + domain.getName() +" must be in status DELETED to wipe off");
		 
		if (domain.getName().equals("kbee"))
			throw new DataManagementException("Domain kbee can not be deleted");

		deleteLogEvent(domain);
		
		
		// LogEvent
		// SendEmailEvent
		// Notification
		// Vote
		// ENotiRule
		// Preferences
		// Labels
		// Resources
		// Contents
		// DataSetValues
		// ContentClasses
		// Classifiers
		// DataSets
		// Rules
		// Groups
		// Users
		// Domain
		// delete kb_preferences
		// delete from kb_file where domain=1

		
		

		
		
		
		

		
		
	}
	

	private void deleteLogEvent(Domain domain)  throws DataManagementException {
	
		
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
