package com.novamens.kbee.content.indexer;

import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;

public class DomainIndexerService extends JavaIndexerService {
	
	public DomainIndexerService(Index index) {
		super(index);
	}
	
	@Override
	protected boolean isApi() {
		return Session.isApi();
	}
}
