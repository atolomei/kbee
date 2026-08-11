package com.novamens.kbee.content.workflow.multidimensional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class AssignedExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		
		OffsetDateTime assigned = null;
		
		Content content = (Content)object;
		
		ContentService contentService =  content.getService(ContentService.class);
		
		assigned = contentService.getAssignationTime();
		
		String value = assigned!=null ? DateTimeFormatter.ISO_INSTANT.format(assigned) : null;
		
		return value;
	}
}
