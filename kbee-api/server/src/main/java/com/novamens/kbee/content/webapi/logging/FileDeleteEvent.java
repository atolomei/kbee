package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import kbee.api.model.ApiFile;
import kbee.api.model.IError;
import kbee.api.model.ITransaction;

public class FileDeleteEvent extends FileEvent {
	
	private long start;

	public FileDeleteEvent(String uri) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(uri);
		setMethod("DELETE");
		start = System.currentTimeMillis();
	}
	
	public void setFile(ApiFile file) {
		setFile(file.getExternalId());
		setRequest(toJson(file));
		setDomain(file.getDomain());
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
