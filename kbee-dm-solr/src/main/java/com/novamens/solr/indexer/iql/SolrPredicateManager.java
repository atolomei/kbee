package com.novamens.solr.indexer.iql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;

import com.novamens.indexer.iql.Predicate;
import com.novamens.indexer.iql.PredicateManager;


/**
 * 
 * 
 *
 */
public class SolrPredicateManager extends PredicateManager implements BeanNameAware  {
	private String beanName;
	private Map<String, Predicate> predicates = new HashMap<String, Predicate>();

	public SolrPredicateManager() {
	}
	
	@Override
	public Predicate getPredicate(String name) {
		return predicates.get(name.toLowerCase());
	}
	
	public void setPredicates(List<Predicate> predicates) {
		for (Predicate predicate : predicates) {
			this.predicates.put(predicate.getName().toLowerCase(), predicate);
		}	
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public String getBeanName() {
		return this.beanName;
	}

	
	@Override
	public List<Predicate> getModelPredicates() {
		return null;
	}

	@Override
	public List<Predicate> getPredicates() {
		return null;
	}
	
	
	public void reset() {
	}
		
}
