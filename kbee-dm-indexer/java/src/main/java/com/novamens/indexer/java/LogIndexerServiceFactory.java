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
			 
public class LogIndexerServiceFactory extends AbstractServiceFactory<IndexerService> {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(LogIndexerService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(Domain.class, object);
		LogIndexerService service = new LogIndexerService(getIndex((Domain)object));
		return (S)service;
	}	
	
	public Index getIndex(Domain domain) {
		return (Index)ServiceLocator.getService(BeansService.class).getBean(domain.getName()+"-audit-index");
	}
}
