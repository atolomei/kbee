package com.novamens.solr.indexer.query;

import java.util.Map;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.service.Index;


public class SolrQueryBuilder extends QueryBuilder {
	private static final long serialVersionUID = 1L;
	private Index index;
	
	public SolrQueryBuilder(Index index) {
		this.index = index;
	}
	
	public Query build(String text) {
		return null;
	};
	
	public Query build(Map<String, Object> parameters) {
		SolrQuery query = new SolrParametersQuery(index);
		query.setParameters(parameters);
		query.setIncludeFacets(true);
		return query;
	};
}
