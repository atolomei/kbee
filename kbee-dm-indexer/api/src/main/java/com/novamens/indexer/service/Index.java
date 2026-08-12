package com.novamens.indexer.service;

import java.io.Serializable;

import com.novamens.indexer.query.TextQuery;
import com.novamens.security.audit.AuditSet;

public interface Index extends com.novamens.dom.Object {
	public void indexDocument(Document document) throws IndexerException;
	public void reindexDocument(Document document, String...field) throws IndexerException;
	public void delete(Serializable id) throws IndexerException;
	public void commit() throws IndexerException;
	public Object execute(TextQuery query) throws IndexerException;
	public Cube getCube();
	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}
