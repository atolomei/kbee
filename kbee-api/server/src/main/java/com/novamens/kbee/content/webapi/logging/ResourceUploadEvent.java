package com.novamens.kbee.content.webapi.logging;

import org.springframework.http.HttpStatus;

import kbee.api.model.ITransaction;

public class ResourceUploadEvent extends ResourceEvent {
	
	private long start;
	
	public ResourceUploadEvent(String uri, String domain, String document, String filename) {
		setUri(uri);
		setDomain(domain);
		setFile(document);
		setRequest(filename);
		start = System.currentTimeMillis();
	}
	
	public void setResponse(ITransaction transaction) {
		long now = System.currentTimeMillis();
		setProcessingTime(now-start);
		setTransaction(transaction.getId());
		setStatus(HttpStatus.OK);
		super.setResponse(toJson(transaction));
	}
}
