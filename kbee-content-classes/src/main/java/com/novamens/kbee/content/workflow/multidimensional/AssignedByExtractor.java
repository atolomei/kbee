package com.novamens.kbee.content.workflow.multidimensional;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class AssignedByExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		
		String assignedBy = null;
		
		Content content = (Content)object;
		
		ContentService contentService =  content.getService(ContentService.class);
		
		assignedBy = contentService.getSender();
		
		return assignedBy;
	}
}
