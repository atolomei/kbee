package com.novamens.kbee.content.workflow.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class TaskExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		List<String> members = new ArrayList<String>();
		
		Content content = (Content)object;
		
		WorkflowService workflowService = content.getService(WorkflowService.class);
		if (workflowService==null || workflowService.getTask()==null) return members;
		
		members.add(workflowService.getTask().getName());
		
		return members;
	}
}
