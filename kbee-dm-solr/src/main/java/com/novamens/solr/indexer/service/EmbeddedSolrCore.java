package com.novamens.solr.indexer.service;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.novamens.indexer.service.IndexerException;

//NOTE: REQUIRES COMMENTED MAVEN DEPENDENCY solr-core
public class EmbeddedSolrCore /*implements SolrCore*/ {
/*	private EmbeddedSolrServer solrCore;
	private boolean initialized = false;
	private String directory;
	private String name;
	private SolrServer solrServer;
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public String getDirectory() {
		return this.directory;
	}
	
	public void setName(String core) {
		this.name = core;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setServer(SolrServer server) {
		this.solrServer = server;
	}
	
	public synchronized void initialize() throws IndexerException {
		if (initialized) return;
		solrCore = new EmbeddedSolrServer(solrServer.getCores(), name);
		initialized = true;
	}
	
	public void add(SolrInputDocument solrdoc) throws SolrServerException, IOException, IndexerException {
		@SuppressWarnings("unused")
		UpdateResponse response = getCore().add(solrdoc);
	}
	
	public void deleteById(String id) throws SolrServerException, IOException, IndexerException {
		getCore().deleteById(id);
	}
	
	public QueryResponse query(SolrQuery query) throws SolrServerException, IOException, IndexerException {
		return getCore().query(query);
	};
	
	public void commit() throws SolrServerException, IOException, IndexerException {
		getCore().commit(false, false, true);
	}
	
	public EmbeddedSolrServer getCore() throws IndexerException {
		if (!initialized) 
			initialize();
		return solrCore;
	}
*/
}
