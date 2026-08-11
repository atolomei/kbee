package com.novamens.kbee.content.webapi.type;

import com.novamens.workflow.Process;

import kbee.api.model.ApiProcess;
import kbee.api.model.ApiProxy;

import com.novamens.workflow.Procedure;

public class IProcessAdapter implements Adapter<Process, ApiProcess> {
	
	public IProcessAdapter() {
	}
	
	public ApiProcess adapt(Process process) {
		ApiProcess iprocess = new ApiProcess();
		
		iprocess.setId(String.valueOf(process.getId()));
		iprocess.setStartTime(process.getStartTime());
		iprocess.setState(process.getStatus().name());
		iprocess.setEndime(process.getEndTime());
		Procedure procedure = process.getProcedure();
		iprocess.setProcedure(new ApiProxy(String.valueOf(procedure.getId()), procedure.getDisplayName(), UriHelper.getUri(procedure), "procedure"));
		return iprocess;
	}
}