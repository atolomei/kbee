 package com.novamens.indexer.iql;

import com.novamens.indexer.service.IndexerException;

public class PredicateNotFoundException extends IndexerException {
	private static final long serialVersionUID = 1L;

	public PredicateNotFoundException(String predicateName)	{
		super("Predicate " + predicateName + " not found.");
	}
}
