package com.novamens.kbee.content.multidimensional;

import com.novamens.content.base.Content;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class KnowledgeBaseExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		if (!(object instanceof Content)) return "false"; 
		Content content = (Content)object;
		return content.getContentTemplate().isKnowledgeBaseCabinet() ? "true" : "false";
	}
}
