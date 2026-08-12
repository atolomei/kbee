package com.novamens.solr.indexer.iql;

import com.novamens.indexer.iql.CalculatedPredicate;

public class MemberPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	public MemberPredicate() {
		setValueTypeDescription("Member Number");
	}
	
	public String getCode(String argument) {
		return "groupmember:" + argument;
	}
	
	public boolean isCanonical() {
		return true;
	}
	
	public boolean evaluate(Object object, Object argument) {
		boolean evaluation = false;
		return evaluation;
	}
}
