package com.novamens.kbee.content.iql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.DomainProxy;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.iql.Predicate;
import com.novamens.indexer.iql.PredicateManager;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.IndexerService;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.FieldPredicate;
import com.novamens.solr.indexer.iql.MemberPredicate;
import com.novamens.solr.indexer.iql.SolrAfterPublishPredicate;
import com.novamens.solr.indexer.iql.SolrDataSetPredicate;
import com.novamens.solr.indexer.iql.SolrIqlService;
import com.novamens.solr.indexer.iql.SolrRolePredicate;
import com.novamens.solr.indexer.iql.SolrValuePredicate;
import com.novamens.solr.indexer.iql.TitlePredicate;
import com.novamens.solr.indexer.iql.WriteablePredicate;

			 

/**
 * 
 * <p>Creates Predicates and give them to the manager to have</p>
 * 
 *
 */
public class KbeeIqlServiceFactory extends AbstractServiceFactory<IndexerService> implements EventListener  {

	// TODO HA
	//
	private Map<Serializable, PredicateManager> cache = Collections.synchronizedMap(new HashMap<Serializable, PredicateManager>());

	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(IqlService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(Domain.class, object);
		Domain domain = (Domain)object;
		JavaIndexerService indexer = domain.getService(JavaIndexerService.class);
		SolrIqlService service = new SolrIqlService(indexer.getIndex(), new DomainProxy(domain));
		service.setPredicateManager(getPredicateManager(domain));
		return (S)service;
	}	
	
	public PredicateManager getPredicateManager(Domain domain) {
		PredicateManager manager = cache.get(domain.getId());
		if (manager == null) {
			synchronized (domain) {
				manager = getManagerBean(domain); 
				if (manager == null) {
					manager = createManager(domain);
				}
					
				this.cache.put(domain.getId(), manager);
			}
		}
		return manager;
	}
	
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return event.getObject() instanceof Classifier || 
			event.getObject() instanceof Attribute;
	}
	
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent) {
			this.cache.clear();	
		}	
		else if (event.getObject() instanceof Classifier) {
			this.cache.remove(((Classifier)event.getObject()).getDomain().getId());
		}
		else if (event.getObject() instanceof Attribute) {
			this.cache.remove(((Attribute)event.getObject()).getDomain().getId());
		}
	}
	
	private PredicateManager getManagerBean(Domain domain) {
 		if (!ServiceLocator.getService(BeansService.class).containsBean(domain.getName()+"-predicate-manager"))
			return null;
		else
			return (PredicateManager)ServiceLocator.getService(BeansService.class).getBean(domain.getName()+"-predicate-manager");
 	}
	
	
	/**
	 * Canonical Predicates  (they do not depend on the Model)
	 * 
	 * @param domain
	 * @return
	 */
	private PredicateManager createManager(Domain domain) {
		
		KbeePredicateManager manager = new KbeePredicateManager();
		manager.setDomain(domain);
		manager.setContentDao(getContentDao());
		
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		SolrValuePredicate predicate = new SolrValuePredicate();
		predicate.setName("domain");
		predicate.setJPath("domain/id");
		predicate.setPath("domain");
		predicate.setCanonical(true);
		predicate.setValueTypeDescription("Domain Id");
		predicates.add(predicate);
		
		
		// Library
		SolrValuePredicate kbpredicate = new SolrValuePredicate();
		kbpredicate.setName("iskbase");
		kbpredicate.setJPath("contentTemplate/knowledgeBaseCabinet");
		kbpredicate.setPath("isknowledgebase");
		kbpredicate.setIsLibrary(true);
		kbpredicate.setValueTypeDescription("boolean");
		kbpredicate.setCanonical(false);
		predicates.add(kbpredicate);
		
		SolrValuePredicate knowledgebasepredicate = new SolrValuePredicate();
		knowledgebasepredicate.setName("isknowledgebase");
		knowledgebasepredicate.setJPath("contentTemplate/knowledgeBaseCabinet");
		knowledgebasepredicate.setPath("isknowledgebase");
		knowledgebasepredicate.setCanonical(false);
		knowledgebasepredicate.setIsLibrary(true);
		knowledgebasepredicate.setValueTypeDescription("boolean");
		predicates.add(knowledgebasepredicate);
		
		SolrValuePredicate templatePredicate = new SolrValuePredicate();
		templatePredicate.setName("istemplate");
		templatePredicate.setJPath("contentTemplate/isTemplate");
		templatePredicate.setPath("istemplate");
		templatePredicate.setCanonical(false);
		templatePredicate.setIsLibrary(true);
		templatePredicate.setValueTypeDescription("boolean");
		predicates.add(templatePredicate);

		SolrValuePredicate externalPredicate = new SolrValuePredicate();
		externalPredicate.setName("isexternal");
		externalPredicate.setJPath("isExternal");
		externalPredicate.setPath("isexternal");
		externalPredicate.setIsLibrary(true);
		externalPredicate.setCanonical(false);
		externalPredicate.setValueTypeDescription("boolean");
		predicates.add(externalPredicate);
		
		
		// ----
		//
		SolrValuePredicate headPredicate = new SolrValuePredicate();
		headPredicate.setName("ishead");
		headPredicate.setJPath("isHeadVersion");
		headPredicate.setPath("head");
		headPredicate.setCanonical(true);
		headPredicate.setValueTypeDescription("boolean");
		predicates.add(headPredicate);

		TitlePredicate namepredicate = new TitlePredicate();
		namepredicate.setName("name");
		predicates.add(namepredicate);
		
		MemberPredicate memberpredicate = new MemberPredicate();
		memberpredicate.setName("member");
		predicates.add(memberpredicate);
		
		TitlePredicate titlepredicate = new TitlePredicate();
		titlepredicate.setName("title");
		predicates.add(titlepredicate);
		
		FieldPredicate opredicate = new FieldPredicate("objectid");
		opredicate.setName("contentOId");
		opredicate.setJPath("oId");
		opredicate.setCanonical(true);
		opredicate.setValueTypeDescription("OId");
		predicates.add(opredicate);

		predicates.add(new SolrAfterPublishPredicate());
		
		predicates.add(new StatePredicate()); // internalstate
		predicates.add(new UserNamePredicate()); // username
		
		SolrValuePredicate activePredicate = new SolrValuePredicate();
		activePredicate.setName("isactive");
		activePredicate.setPath("active");
		activePredicate.setCanonical(true);
		activePredicate.setValueTypeDescription("boolean");
		predicates.add(activePredicate);
		
		predicates.add(new ContentClassPredicate()); // contentclass
		
		ContentClassPredicate templatepredicate = new ContentClassPredicate();
		templatepredicate.setName("contentTemplate");
		predicates.add(templatepredicate); // contentTemplate
		
		SolrDataSetPredicate datasetpredicate = new SolrDataSetPredicate();
		datasetpredicate.setName("dataset");
		datasetpredicate.setDomain(domain);
		predicates.add(datasetpredicate ); 
		
		WriteablePredicate writeablepredicate = new WriteablePredicate();
		writeablepredicate.setName("writeable");
		predicates.add(writeablepredicate);

		SolrRolePredicate rolepredicate = new SolrRolePredicate();
		rolepredicate.setName("role");
		predicates.add(rolepredicate);

		predicates.add(new SignedPredicate());
		
		manager.setPredicates(predicates);
		
		return manager;
	}
	
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		return dao;
	}
}
