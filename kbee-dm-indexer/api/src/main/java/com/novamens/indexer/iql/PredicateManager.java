package com.novamens.indexer.iql;

import java.util.List;

public abstract class PredicateManager {
	
	private static PredicateManager Instance;
	
	public static PredicateManager getInstance() {
		return Instance;
	}
	
	public static void setInstance(PredicateManager manager) { 
		Instance = manager;
	}
	
	public Predicate getPredicate(String name) {
		return null;
	}
	
	public String getBeanName() {
		return null;
	}
	
	public abstract List<Predicate> getModelPredicates();
	public abstract List<Predicate> getPredicates();
	
	public abstract void reset();
	
}
