package com.novamens.kbee.content.workflow.multidimensional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.content.workflow.KbeeContext;

public class DueDateExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		
		Content content = (Content)object;
		
		WorkflowService workflowService = content.getService(WorkflowService.class);
		if (workflowService==null || 
				workflowService.getTask()==null || 
				workflowService.getContext().getTime()==null) {
			return null;
		}	
		
		KbeeContext context = (KbeeContext)workflowService.getContext();
		OffsetDateTime duedate = context.getDueDate();
		
		String datestring;
		if (duedate!=null) {
			DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
			datestring = formatter.format(duedate);
		}
		else {
			datestring = "9999-99-99T99:99:99.999-99:99";
		}
		
		return datestring;
	}
}
