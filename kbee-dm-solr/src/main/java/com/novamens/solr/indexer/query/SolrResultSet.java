package com.novamens.solr.indexer.query;

import org.apache.solr.client.solrj.response.QueryResponse;

import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.solr.indexer.service.SolrIndex;

public interface SolrResultSet extends ResultSet {
	public static int PAGE_SIZE = 60;
	public QueryResponse getQueryResponse();
	public Query getQuery();
	public void setPageSize(int size);
	public SolrIndex getIndex();
}
