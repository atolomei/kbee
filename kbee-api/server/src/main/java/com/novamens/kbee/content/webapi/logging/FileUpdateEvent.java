package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import com.novamens.kbee.content.webapi.type.UriHelper;

import kbee.api.model.ApiFile;
import kbee.api.model.IError;
import kbee.api.model.ITransaction;

public class FileUpdateEvent extends FileEvent {
	
	private long start;

	public FileUpdateEvent() {
	}
	
	public FileUpdateEvent(ApiFile file) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(UriHelper.getUri(file));
		setDomain(file.getDomain());
		setFileSource(file.getApplication());
		setFile(file.getExternalId());
		setMethod("POST");
		setRequest(toJson(file));
		setContentClass(file.getClassName());
		start = System.currentTimeMillis();
	}
	
	public FileUpdateEvent(ApiFile file, ITransaction transaction) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(UriHelper.getUri(file));
		setFileSource(file.getApplication());
		setFile(file.getExternalId());
		setTransaction(transaction.getId());
		setMethod("POST");
		setStatus(HttpStatus.OK);
		setRequest(toJson(file));
		setDomain(file.getDomain());
		setContentClass(file.getClassName());
		setResponse(toJson(transaction));
	}
	
	public FileUpdateEvent(ApiFile file, IError error, HttpStatus status) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(UriHelper.getUri(file));
		setFileSource(file.getApplication());
		setFile(file.getExternalId());
		setMethod("POST");
		setStatus(status);
		setRequest(toJson(file));
		setDomain(file.getDomain());
		setContentClass(file.getClassName());
		setResponse(toJson(error));
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
