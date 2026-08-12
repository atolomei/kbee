package com.novamens.solr.indexer.service;

import org.springframework.util.Assert;

import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
			 
public class SolrIndexerServiceFactory extends AbstractServiceFactory<IndexerService> {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(JavaIndexerService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(SolrIndex.class, object);
		SolrIndexerService service = new SolrIndexerService((Index)object);
		return (S)service;
	}	
}
