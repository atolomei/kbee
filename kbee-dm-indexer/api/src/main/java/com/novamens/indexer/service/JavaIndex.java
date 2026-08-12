package com.novamens.indexer.service;

public interface JavaIndex extends Index {
	public void index(Object object) throws IndexerException;
	public void index(Object object, boolean onlymetainfo) throws IndexerException;
	public void index(Object object, boolean onlymetainfo, boolean aggregations) throws IndexerException;
	public void index(Object object, boolean onlymetainfo, boolean aggregations, boolean force) throws IndexerException;
	public boolean isIndexable(Object object);
	public ObjectBuilder getObjectBuilder();
}
