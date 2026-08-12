package com.novamens.solr.indexer.service;

import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;

public class SolrIndexerService extends JavaIndexerService {
	
	public SolrIndexerService(Index index) {
		super(index);
	}
	
	protected boolean isApi() {
		return Session.isApi();
	}
}
