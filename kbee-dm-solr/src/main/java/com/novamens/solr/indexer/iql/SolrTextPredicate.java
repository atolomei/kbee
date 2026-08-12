package com.novamens.solr.indexer.iql;

import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.indexer.query.SemanticEngine;

public class SolrTextPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	private SemanticEngine semanticEngine;
	
	public String getCode(String argument) {
		return null;
	}
	
	public boolean evaluate(Object object, Object argument) {
		return false;
	}
	
	public SemanticEngine getSemanticEngine() {
		return semanticEngine;
	}

	public void setSemanticEngine(SemanticEngine engine) {
		semanticEngine = engine;
	}
	
	public boolean isCanonical() {
		return false;
	}

	
}
