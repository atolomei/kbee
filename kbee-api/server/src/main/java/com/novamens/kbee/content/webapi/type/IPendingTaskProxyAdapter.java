package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Task;

import kbee.api.model.IPendingTaskProxy;

public class IPendingTaskProxyAdapter implements Adapter<Content, IPendingTaskProxy> {
	
	public IPendingTaskProxyAdapter() {
	}
	
	public IPendingTaskProxy adapt(Content content) {
		IPendingTaskProxy proxy = new IPendingTaskProxy();
		Task task = content.getService(WorkflowService.class).getTask();
		if (task==null) return null;
		proxy.setId(String.valueOf(content.getId()));
		proxy.setRel("workitem");
		proxy.setName(getTitle(content));
		proxy.setTask(task.getDisplayName());
		
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
		String timestring = ServiceLocator.getService(DateTimeService.class).timeElapsed(context.getContent().getLastModifiedOffsetDateTime());
		proxy.setTime(timestring);

		return proxy;	
	}
	
	private String getTitle(Content content) {
		String title = content.getTitle();
		if (title==null) return "";
		title = title.replace("\r", "");
		title = title.replace("\n", "");
		return title;
	}
}