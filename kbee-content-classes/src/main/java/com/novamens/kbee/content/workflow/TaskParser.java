package com.novamens.kbee.content.workflow;

import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

public abstract class TaskParser {
	
	public abstract Json getJson(List<Task> tasks);
	public abstract List<Task> getTasks(Json json, Procedure procedure);
	
	public static TaskParser Get() {
		return (TaskParser)ServiceLocator.getService(BeansService.class).getBean("WorkflowTaskParser");
	}
}
