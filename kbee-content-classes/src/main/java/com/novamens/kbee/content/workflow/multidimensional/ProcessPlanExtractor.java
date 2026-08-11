package com.novamens.kbee.content.workflow.multidimensional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.content.workflow.KbeeProcedureBean;

public class ProcessPlanExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		List<String> members = new ArrayList<String>();
		
		Content content = (Content)object;
		
		WorkflowService workflowService = content.getService(WorkflowService.class);
		if (workflowService==null || workflowService.getTask()==null) return members;
		
		
		
		
		// TODO AJUSTAR ESTO A JAVA 8
		//
		long startTime = workflowService.getContext().getProcess().getStartTime().toInstant().toEpochMilli();
		long currentTime = Instant.now().toEpochMilli();
		long elpasedTime = currentTime - startTime;
		long hoursSpent = elpasedTime / (3600000L);
		
		
		if (hoursSpent>((KbeeProcedureBean)workflowService.getContext().getProcedure()).getPlannedTime()) {
			members.add("Delayed");
		}
		else {
			members.add("On Time");
		}
		
		return members;
	}
}
