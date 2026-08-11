package com.novamens.kbee.content.workflow;

import com.novamens.workflow.ProcedurePhase;

import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;

public abstract class PhaseParser {
	
	public abstract Json getJson(List<ProcedurePhase> phases);
	public abstract List<ProcedurePhase> getPhases(Json phases);
	
	public static PhaseParser Get() {
		return (PhaseParser)ServiceLocator.getService(BeansService.class).getBean("ProcedurePhaseParser");
	}
}