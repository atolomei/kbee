package com.novamens.kbee.content.indexer;

import org.springframework.util.Assert;

import com.novamens.content.base.DomainProxy;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
			 
public class LogIndexerServiceFactory extends AbstractServiceFactory<IndexerService> {
	
	private JavaIndexFactory factory;
	
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
		return new IndexProxy(getIndexFactory().getIndex(domain), getIndexFactory(), new DomainProxy(domain));
	}
	
	public JavaIndexFactory getIndexFactory() {
		return this.factory;
	}
	
	public void setIndexFactory(JavaIndexFactory factory) {
		this.factory = factory;
	}
}
