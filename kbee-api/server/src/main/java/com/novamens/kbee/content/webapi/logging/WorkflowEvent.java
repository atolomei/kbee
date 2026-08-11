package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import com.novamens.kbee.content.webapi.type.UriHelper;

import kbee.api.model.IError;
import kbee.api.model.ITransaction;
import kbee.api.model.IWorkflowEvent;

public class WorkflowEvent extends AbstractApiLogEvent {
	
	private long start;
	
	public WorkflowEvent(IWorkflowEvent event) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(UriHelper.getUri(event));
		setDomain(event.getDomain());
		//setFileSource(file.getApplication());
		//setFile(file.getExternalId());
		setMethod("POST");
		setRequest(toJson(event));
		//setContentClass(file.getClassName());
		start = System.currentTimeMillis();
	}
	
	public void setResponse(ITransaction transaction) {
		long now = System.currentTimeMillis();
		setProcessingTime(now-start);
		setTransaction(transaction.getId());
		setStatus(HttpStatus.OK);
		super.setResponse(toJson(transaction));
	}
	
	public void setResponse(IError error) {
		long now = System.currentTimeMillis();
		setProcessingTime(now-start);
		super.setResponse(toJson(error));
	}
}
