package com.novamens.solr.indexer.util;

import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;

public class SolrPortalFileTextExtractor extends SolrFileTextExtractor {
	
	public SolrPortalFileTextExtractor() {
	}
	
	public SolrPortalFileTextExtractor(String path) {
		setPath(path);
	}
	
	@Override
	protected boolean indexable (Object object, KBFile file) {
		if (!(object instanceof ResourceContainer)) 
			return false;
		if (!((ResourceContainer)object).isPublic(file))
			return false;
		if (file.getSize()>(10000000)) {
			return  false;
		}
		if (file.isInPortalVersion())
			return true;
		return false;
	}
}