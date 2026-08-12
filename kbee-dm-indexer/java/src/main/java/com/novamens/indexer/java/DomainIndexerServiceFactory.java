package com.novamens.indexer.java;

import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
			 
public class DomainIndexerServiceFactory extends AbstractServiceFactory<IndexerService> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainIndexerServiceFactory.class.getName());

	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(JavaIndexerService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(Domain.class, object);
		DomainIndexerService service = new DomainIndexerService(getIndex((Domain)object));
		return (S)service;
	}	
	
	public Index getIndex(Domain domain) {
		try {
			//si esto no exitse se crea a mano
			return (Index)ServiceLocator.getService(BeansService.class).getBean(domain.getName()+"-index");
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}
}
