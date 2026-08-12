package com.novamens.solr.indexer.iql;

import com.novamens.indexer.iql.CalculatedPredicate;

import kbee.query.QueryHelpher;

public class WriteablePredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	public WriteablePredicate() {
		setValueTypeDescription("Boolean Value");
	}
	
	public String getCode(String argument) {
		String statement = QueryHelpher.buildSecurityTerm("write");
		if ("".equals(statement)) statement = "reader:*";
		return statement;
	}
	
	public boolean isCanonical() {
		return true;
	}
	
	public boolean evaluate(Object object, Object argument) {
		return false;
	}
}