package com.novamens.indexer.service;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.service.ObjectService;

public interface QueryService extends ObjectService {
	public ResultSet extecute(Query query);
}
