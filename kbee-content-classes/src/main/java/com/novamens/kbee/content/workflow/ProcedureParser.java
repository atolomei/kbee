package com.novamens.kbee.content.workflow;

import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;

public abstract class ProcedureParser {
	
	public abstract Json getJson(List<Procedure> procedures);
	public abstract List<Procedure> getProcedures(Json json, Procedure parent);
	
	public static ProcedureParser Get() {
		return (ProcedureParser)ServiceLocator.getService(BeansService.class).getBean("WorkflowProcedureParser");
	}
}
