package com.novamens.solr.indexer.iql;

import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.iql.PredicateManager;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.IndexerService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
			 
public class SolrIqlServiceFactory extends AbstractServiceFactory<IndexerService> {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(IqlService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(Domain.class, object);
		Domain domain = (Domain)object;
		JavaIndexerService indexer = domain.getService(JavaIndexerService.class);
		SolrIqlService service = new SolrIqlService(indexer.getIndex(), domain);
		service.setPredicateManager(getPredicateManager(domain));
		return (S)service;
	}	
	
	public PredicateManager getPredicateManager(Domain domain) {
		return (PredicateManager)ServiceLocator.getService(BeansService.class).getBean(domain.getName()+"-predicate-manager");
	}
}
