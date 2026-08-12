package com.novamens.solr.indexer.service;

//NOTE: REQUIRES COMMENTED MAVEN DEPENDENCY solr-core
public class SolrServer {
	/*
	private static final String INDEX_DIR = PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.index",  "index");
	
	private boolean initialized = false;
	private String directory = INDEX_DIR;
	private CoreContainer container;
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public String getDirectory() {
		return this.directory;
	}
	
	public synchronized void initialize() throws IndexerException {
		if (initialized) return;
		container = new CoreContainer(getDirectory());
		container.load();
		initialized = true;
	}
	
	public CoreContainer getCores() throws IndexerException {
		if (!initialized) initialize();
		return container;
	}
*/
}
