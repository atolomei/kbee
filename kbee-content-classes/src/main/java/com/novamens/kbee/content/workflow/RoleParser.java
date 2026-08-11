package com.novamens.kbee.content.workflow;

import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.RoleInProcess;

public abstract class RoleParser {
	
	public abstract Json getJson(List<RoleInProcess> roles);
	public abstract List<RoleInProcess> getRoles(Json json);
	
	public static RoleParser Get() {
		return (RoleParser)ServiceLocator.getService(BeansService.class).getBean("WorkflowRoleParser");
	}
}
