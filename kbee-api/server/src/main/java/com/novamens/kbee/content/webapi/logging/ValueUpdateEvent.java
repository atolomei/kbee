package com.novamens.kbee.content.webapi.logging;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import com.novamens.kbee.content.webapi.type.UriHelper;

import kbee.api.model.ApiValue;
import kbee.api.model.IError;
import kbee.api.model.ITransaction;

public class ValueUpdateEvent extends ValueEvent {
	
	private long start;

	public ValueUpdateEvent(ApiValue value) {
		setTime(OffsetDateTime.now());
		setUser();
		setUri(UriHelper.getUri(value));
		setMethod("POST");
		setDomain(value.getDomain());
		setRequest(toJson(value));
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
