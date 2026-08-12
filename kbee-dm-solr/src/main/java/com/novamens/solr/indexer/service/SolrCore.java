package com.novamens.solr.indexer.service;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import com.novamens.indexer.service.IndexerException;

public interface SolrCore {

	public void add(SolrInputDocument solrdoc) throws SolrServerException, IOException, IndexerException;
	
	public void deleteById(String id) throws SolrServerException, IOException, IndexerException;
	
	public void deleteById(List<String> ids) throws SolrServerException, IOException, IndexerException;
	
	public QueryResponse query(SolrQuery query) throws SolrServerException, IOException, IndexerException;
	
	public void commit() throws SolrServerException, IOException, IndexerException;
}
