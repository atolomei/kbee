package com.novamens.indexer.iql;

import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.QueryService;

public interface IqlService extends QueryService {
	
	public IqlQuery getNewQuery(String statement);
	public Expression getExpression(String statement);
	public ResultSet execute(String statement);
	
	PredicateManager getPredicateManager();
}