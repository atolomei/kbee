package com.novamens.kbee.content.iql;

import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.solr.indexer.iql.SolrAbstractPredicate;

public class UserNamePredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	public UserNamePredicate() {
		setName("username");
	}

	@Override
	public String getHelpValueTypeDescription() {
		return 	"username";
	}
	
	public String getCode(String argument) {
		return "(username:" + argument + ")";
	}
	
	public boolean evaluate(Object object, Object argument) {
		return false;
	}
	
	public boolean isCanonical() {
		return true;
	}
}
