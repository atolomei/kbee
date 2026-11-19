package com.novamens.solr.indexer.service;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.novamens.indexer.service.IndexerException;

import kbee.util.PropertiesFactory;

public class SolrCoreClient implements SolrCore {

    SolrClient client;
    private boolean initialized = false;
    private String url;
    private String name;
    
    private String username = PropertiesFactory
    	.getInstance("kbee")
    	.getProperties()
    	.getProperty("solr.username", null);

    private String password = PropertiesFactory
        .getInstance("kbee")
        .getProperties()
        .getProperty("solr.password", null);

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setName(String core) {
        this.name = core;
    }

    public String getName() {
        return this.name;
    }

//    public synchronized void initialize2() throws IndexerException {
//        if (initialized) return;
//        client = new HttpSolrClient.Builder(getUrl() + "/" + getName()).build();
//        initialized = true;
//    }
    
    public synchronized void initialize() throws IndexerException {

        if (initialized) {
            return;
        }
        
        HttpJdkSolrClient.Builder builder =
                new HttpJdkSolrClient.Builder(getUrl())
                        .withDefaultCollection(getName())
                        .useHttp1_1(true);
        
        if (username != null && !username.isBlank()
                && password != null && !password.isBlank()) {
            builder.withBasicAuthCredentials(username, password);
        }

        client = builder.build();

        initialized = true;
    }

    public void add(SolrInputDocument solrdoc) throws SolrServerException, IOException, IndexerException {
        @SuppressWarnings("unused")
        UpdateResponse response = getCore().add(solrdoc);
    }

    public void deleteById(String id) throws SolrServerException, IOException, IndexerException {

        getCore().deleteById(id);
    }

    public void deleteById(List<String> ids) throws SolrServerException, IOException, IndexerException {
        getCore().deleteById(ids);
    }

    public QueryResponse query(SolrQuery query) throws SolrServerException, IOException, IndexerException {
    	return getCore().query(query);
    }
    
    public void commit() throws SolrServerException, IOException, IndexerException {
        getCore().commit(true, true, true);
    }

    public SolrClient getCore() throws IndexerException {
        if (!initialized) 
        	initialize();
        
        return client;
    }


}
