package com.novamens.kbee.content.iql;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.BeanNameAware;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.iql.AttributePredicate;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetType;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.Predicate;
import com.novamens.indexer.iql.PredicateManager;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.solr.indexer.iql.SolrAfterDatePredicate;
import com.novamens.solr.indexer.iql.SolrAttributePredicate;
import com.novamens.solr.indexer.iql.SolrBeforeDatePredicate;
import com.novamens.solr.indexer.iql.SolrClassifierPredicate;
import com.novamens.solr.indexer.iql.SolrGreaterThanPredicate;
import com.novamens.solr.indexer.iql.SolrMemberDao;
import com.novamens.solr.indexer.iql.SolrValidityPredicate;

/**
 * 
 * <p> save the list of predicates </p>
 * <p>The parser calls the Predicate Manager to look for a predicate that is in the clause.</p>
 * <p>Predicate: term of a sentence clause </p> 
 *
 */
public class KbeePredicateManager extends PredicateManager implements BeanNameAware {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePredicateManager.class.getName());
	
	private String beanName;
	private ContentDao contentDao;
	private Domain domain;
	private boolean initialized = false;
	
	private Map<String, Predicate> predicates = new HashMap<String, Predicate>();
	private Map<String, Predicate> predicatesid = new HashMap<String, Predicate>();
	private List<Predicate> predicate_list = null;
	
	
	public void reset() {
		predicate_list=null;
		predicatesid.clear();
		predicates.clear();
		initialized = false;
		initialize();
		
	}
	/**
	 * <b>Canonical</b> 
	 * ContentClass( codeid or id)
	 * 
	 */
	public KbeePredicateManager () {
	}
	
	public void setPredicates(List<Predicate> predicates) {
		for (Predicate predicate : predicates) {
			this.predicates.put(predicate.getName().toLowerCase(), predicate);
		}
	}

	
	
	public List<Predicate> getPredicates() {
	
		if (predicate_list!=null)
			return predicate_list;
		
		predicate_list = new ArrayList<Predicate>();
		
		if (!initialized)
			initialize();
		
		for (Entry<String, Predicate> entry: predicates.entrySet())
			predicate_list.add(entry.getValue());
		
		if (logger.isDebugEnabled())
			predicate_list.forEach( item -> logger.debug(item.getName()));
		
		return predicate_list;
	}

	
	
	private void initialize() {
		if (!initialized) {
			synchronized (this) {
				for (Predicate predicate : getModelPredicates()) {
					this.predicates.put(predicate.getName().toLowerCase(), predicate);
					if (predicate instanceof ClassifierPredicate) {
						String predicatename = "c" + String.valueOf(((ClassifierPredicate)predicate).getClassifier().getId());
						this.predicatesid.put(predicatename, predicate);
					}
				}
				initialized = true;
			}
		}
	}
	
	
	@Override
	public Predicate getPredicate(String name) {
 		if (!initialized) {
			for (Predicate predicate : getModelPredicates()) {
				this.predicates.put(predicate.getName().toLowerCase(), predicate);
				if (predicate instanceof ClassifierPredicate) {
					String predicatename = "c" + String.valueOf(((ClassifierPredicate)predicate).getClassifier().getId());
					this.predicatesid.put(predicatename, predicate);
				}
				if (predicate instanceof AttributePredicate) {
					String predicatename = "a" + String.valueOf(((AttributePredicate)predicate).getAttribute().getId());
					this.predicatesid.put(predicatename, predicate);
				}
			}
			initialized = true;
		}
		
		Predicate predicate = predicates.get(name.toLowerCase());
		if (predicate==null) predicate = predicatesid.get(name.toLowerCase());

		return predicate;
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public String getBeanName() {
		return this.beanName;
	}
	
	
	/**
	 * 
	 * Classifier 
	 * Attribute
	 * 
	 * 
	 * @return
	 */
	public List<Predicate> getModelPredicates() {
		try {
			List<Predicate> predicates = new ArrayList<Predicate>();
			
			for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
				
				if (classifier.getPredicate()!=null && classifier.getDataSet()!=null) {
			
					if (classifier.getDataSetType().equals(DataSetType.DATE)) {
						SolrBeforeDatePredicate predicate = new SolrBeforeDatePredicate();
						predicate.setClassifier(classifier);
						predicate.setName(classifier.getPredicate()+"_beforedays");
						
						predicates.add(predicate);
						
						predicate = new SolrBeforeDatePredicate();
						predicate.setClassifier(classifier);
						predicate.setName(classifier.getPredicate()+"_before");
						predicates.add(predicate);
						
						SolrAfterDatePredicate predicate_after = new SolrAfterDatePredicate();
						predicate_after.setClassifier(classifier);
						predicate_after.setName(classifier.getPredicate()+"_afterdays");
						predicate_after.setValueTypeDescription("from, to");
						predicates.add(predicate_after);
					}
					else {
						SolrClassifierPredicate predicate = new SolrClassifierPredicate();
						
						predicate.setValueTypeDescription("Dataset Value: " + classifier.getDataSet()!=null? classifier.getDataSet().getName() : "null");
						
						predicate.setName(classifier.getPredicate());
						predicate.setPath(classifier.getUniqueName()+"member");
						predicate.setClassifier(classifier);
						SolrMemberDao memberDao = new SolrMemberDao();
						memberDao.setDataSet(classifier.getDataSet());
						memberDao.setIndex(getIndex());
						predicate.setMemberDao(memberDao);
						predicates.add(predicate);
					}
				}
			}

			
			/**
			 * Attributes 
			 * 
			 * String: String
			 * Date: before_days
			 * 
			 */
			Attribute validityfrom = null, validityto = null;
			
			for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
				
				if (attribute.getType().equals(AttributeType.VALIDITY_FROM)) {
					validityfrom =  attribute; 
				}
				
				if (attribute.getType().equals(AttributeType.VALIDITY_TO)) {
					validityto =  attribute; 
				}
				
				if (attribute.getPredicate()!=null && !"".equals(attribute.getPredicate())) {
					
					if (attribute.isDate()) {
						SolrAttributePredicate predicate = new SolrAttributePredicate();
						predicate.setAttribute(attribute);
						predicate.setName(attribute.getPredicate());
						predicates.add(predicate);
						
						SolrBeforeDatePredicate beforepredicate = new SolrBeforeDatePredicate();
						beforepredicate.setAttribute(attribute);
						beforepredicate.setName(attribute.getPredicate()+"_beforedays");
						beforepredicate.setValueTypeDescription("from, to");
						predicates.add(beforepredicate);
						
						SolrAfterDatePredicate afterpredicate = new SolrAfterDatePredicate();
						afterpredicate.setAttribute(attribute);
						afterpredicate.setName(attribute.getPredicate()+"_afterdays");
						afterpredicate.setValueTypeDescription("from, to");
						predicates.add(afterpredicate);
						
					}
					else 
					if (attribute.getType().equals(AttributeType.NUMBER) ||	 attribute.getType().equals(AttributeType.FLOAT)) {
						SolrGreaterThanPredicate predicate = new SolrGreaterThanPredicate();
						predicate.setAttribute(attribute);
						predicate.setName(attribute.getPredicate()+"_greaterthan");
						predicates.add(predicate);
					}	
					else {
						SolrAttributePredicate predicate = new SolrAttributePredicate();
						predicate.setAttribute(attribute);
						predicate.setName(attribute.getPredicate());
						predicates.add(predicate);
					}
				}
			}
			
			
			/**
			 * Validity range
			 */
			if (validityfrom!=null && validityto!=null) {
				
				SolrValidityPredicate predicate = new SolrValidityPredicate(validityfrom, validityto);
				predicate.setName("validOn");
				predicates.add(predicate);
			}
			return predicates;
		}
		finally {
		}
	}
	
	
	
	public JavaIndex getIndex() {
		return (JavaIndex)getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public void setContentDao(ContentDao dao) {
		this.contentDao = dao;
	}
	
	public ContentDao getContentDao() {
		return contentDao;
	}
}

