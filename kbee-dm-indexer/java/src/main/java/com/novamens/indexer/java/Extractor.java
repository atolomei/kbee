package com.novamens.indexer.java;

import com.novamens.indexer.service.IndexerException;

public interface Extractor {
	public Object extract(Object object) throws IndexerException;
}
